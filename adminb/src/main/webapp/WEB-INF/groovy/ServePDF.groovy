import com.google.appengine.api.datastore.Entity
import org.calontir.marshallate.falcon.dto.Fighter
import org.calontir.marshallate.falcon.db.FighterDAO
import org.calontir.marshallate.falcon.print.CardMaker
import org.joda.time.DateTime
import com.google.appengine.api.datastore.*
import static com.google.appengine.api.datastore.FetchOptions.Builder.*
import java.util.HashMap

Entity fighter = new Entity("Fighter");

fighter << params

def dao = new FighterDAO()
def f = dao.getFighter(fighter.fighterId.toLong())
def startDate
def endDate

namespace.of("system") {
	name = "calontir.validStart"
	def query = new Query("properties")
	query.addFilter("name", Query.FilterOperator.EQUAL, name)
	PreparedQuery preparedQuery = datastore.prepare(query)
	def entities = preparedQuery.asList( withLimit(10) )
	def entity = entities[0]
	startDate = new Date().parse("MM/dd/yyyy", entity.property)

	name = "calontir.validEnd"
	query = new Query("properties")
	query.addFilter("name", Query.FilterOperator.EQUAL, name)
	preparedQuery = datastore.prepare(query)
	entities = preparedQuery.asList( withLimit(10) )
	entity = entities[0]
	endDate = new Date().parse("MM/dd/yyyy", entity.property)
}

ByteArrayOutputStream baosPDF = new ByteArrayOutputStream()
CardMaker cardMaker = new CardMaker()
List<Fighter> flist = new ArrayList<Fighter>()
flist.add(f)
try {
	cardMaker.build(baosPDF, flist, new DateTime(startDate), new DateTime(endDate))
} catch (Exception ex) {
	throw new IOException("Error building the cards", ex)
}

response.addHeader("Content-Disposition", String.format("attachment;filename=\"FighterCard_%s%ty%tm%td.pdf\"",
        f.getScaName().replaceAll(" ", "_"), today, today, today));

response.contentType = "application/pdf"
response.contentLength = baosPDF.size()

sout << baosPDF
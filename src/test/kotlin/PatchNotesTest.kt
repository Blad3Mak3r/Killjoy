import dev.killjoy.apis.news.PatchNotesAPI
import dev.killjoy.i18n.I18n
import kotlinx.coroutines.runBlocking
import org.junit.Test

class PatchNotesTest {

    private val locales = I18n.VALID_LOCALES

    @Test
    fun `Get latest patch notes`() = runBlocking {
        for (locale in locales) {
            println("Testing patch notes for ${locale.language}")
            val article = PatchNotesAPI.latest(locale)
            println(article)
        }
    }
}
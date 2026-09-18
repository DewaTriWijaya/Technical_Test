package com.dewa.technicaltest.utils

/* Before
object UserManager {
    var activity: Activity? = null
    fun setActivity(activity: Activity) {
        this.activity = activity
    }
}
 */

/* After 1
object UserManager {
    var userId: Int? = null
    var userName: String? = null
}
 */

/* After 2
@Singleton
class UserManager @Inject constructor(
    @ApplicationContext private val context: Context
)
 */



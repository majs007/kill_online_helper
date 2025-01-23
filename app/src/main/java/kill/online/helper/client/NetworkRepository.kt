package kill.online.helper.client


import kill.online.helper.client.route.AppClient
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 通过网络IO读写数据
 */
object NetworkRepository {
    //创建拦截器
    private val appInterceptor = Interceptor { chain ->
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        val url = request.url
        val builder = url.newBuilder()
        requestBuilder.url(builder.build())
            .method(request.method, request.body)
            .addHeader("clientType", "android")
            .addHeader("Content-Type", "application/json")
        chain.proceed(requestBuilder.build())
    }

    //创建OKhttp
    private val appHttpClient: OkHttpClient.Builder = OkHttpClient.Builder()
        .addInterceptor(appInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)


    private val appRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://127.0.0.1/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(appHttpClient.build())
        .build()

    val appClient: AppClient = appRetrofit.create(AppClient::class.java)

}



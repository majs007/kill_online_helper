package kill.online.helper.repository


import kill.online.helper.utils.NetworkUtils
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.X509TrustManager

/**
 * 通过网络IO读写数据
 */
object NetworkRepository {
    //创建拦截器
    private val interceptor = Interceptor { chain ->
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        val url = request.url()
        val builder = url.newBuilder()
        requestBuilder.url(builder.build())
            .method(request.method(), request.body())
            .addHeader("clientType", "IOS")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Authorization", token)
        chain.proceed(requestBuilder.build())
    }

    //创建OKhttp
    private val client: OkHttpClient.Builder = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .retryOnConnectionFailure(false)
        // 添加信任所有证书的 SSL Socket Factory
        .sslSocketFactory(
            NetworkUtils.sSLSocketFactory,
            NetworkUtils.trustManager[0] as X509TrustManager
        )
        // 忽略 Hostname 验证
        .hostnameVerifier { _, _ -> true }


    private val zeroRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.zerotier.com/api/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client.build())
        .build()
    private val appRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client.build())
        .build()

    val zeroTier: ZeroTierClient = zeroRetrofit.create(ZeroTierClient::class.java)
    val appClient: AppClient = appRetrofit.create(AppClient::class.java)

}



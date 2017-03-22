package im.hch.sleeprecord.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import im.hch.sleeprecord.R;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    public static final String WECHAT_LOGIN_RES_ACTION = "wechat.login.response";

    public static final String EXTRA_ERR_CODE = "errCode";
    public static final String EXTRA_ERR_STR = "errStr";
    public static final String EXTRA_CODE = "code";
    public static final String EXTRA_STATE = "state";

    private IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Handle any communication from WeChat and then terminate activity. This class must be an activity
        // or the communication will not be received from WeChat.
        api = WXAPIFactory.createWXAPI(this, getString(R.string.wechat_app_id) , false);
        api.handleIntent(getIntent(), this);

        finish();
    }

    /**
     * Called when wechat initiating a request.
     * @param baseReq
     */
    @Override
    public void onReq(BaseReq baseReq) {

    }

    /**
     * Called when received a wechat response.
     * @param baseResp
     */
    @Override
    public void onResp(BaseResp baseResp) {
        Intent intent = new Intent(WECHAT_LOGIN_RES_ACTION);
        intent.putExtra(EXTRA_ERR_CODE, baseResp.errCode);
        intent.putExtra(EXTRA_ERR_STR, baseResp.errStr);

        if (baseResp instanceof SendAuth.Resp) {
            SendAuth.Resp sendResp = (SendAuth.Resp) baseResp;
            intent.putExtra(EXTRA_CODE, sendResp.code);
            intent.putExtra(EXTRA_STATE, sendResp.state);
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}

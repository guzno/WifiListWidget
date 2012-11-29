package se.magnulund.android.wifilistwidget.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created with IntelliJ IDEA.
 * User: Gustav
 * Date: 29/11/2012
 * Time: 22:22
 * To change this template use File | Settings | File Templates.
 */
public class WifiWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WifiWidgetRemoteViewsFactory(this.getApplicationContext(), intent);  //To change body of implemented methods use File | Settings | File Templates.
    }
}

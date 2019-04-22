package fr.g123k.flutterappbadger;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.tot.badges.IconBadgeNumManager;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * FlutterAppBadgerPlugin
 */
public class FlutterAppBadgerPlugin implements MethodCallHandler {

  private final Context context;

  private FlutterAppBadgerPlugin(Context context) {
    this.context = context;
  }

  /**
   * Plugin registration.
   */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "g123k/flutter_app_badger");
    channel.setMethodCallHandler(new FlutterAppBadgerPlugin(registrar.activeContext()));
  }
  @TargetApi(Build.VERSION_CODES.O)
  private static NotificationChannel createNotificationChannel() {
    String channelId = "test";
    NotificationChannel channel = null;
    channel = new NotificationChannel(channelId,
            "Channel1", NotificationManager.IMPORTANCE_DEFAULT);
    channel.enableLights(true); //是否在桌面icon右上角展示小红点
    channel.setLightColor(Color.RED); //小红点颜色
    channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
    return channel;
  }
  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("updateBadgeCount")) {
      String notificationChannelId = null;
      NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      if (nm == null) return;
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        NotificationChannel notificationChannel = createNotificationChannel();
        nm.createNotificationChannel(notificationChannel);
        notificationChannelId = notificationChannel.getId();
      }
      Notification notification;
      try {
        notification = new NotificationCompat.Builder(context, notificationChannelId)
                .setSmallIcon(context.getApplicationInfo().icon)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("title")
                .setContentText("您有一条新的消息！ ")
                .setTicker("ticker")
                .setAutoCancel(true)
                .setNumber(1)
                .build();
        new IconBadgeNumManager().setIconBadgeNum(((Activity) context).getApplication(), notification, Integer.valueOf(call.argument("count").toString()));
      } catch (Exception e) {
        e.printStackTrace();
      }
      ShortcutBadger.applyCount(context, Integer.valueOf(call.argument("count").toString()));
      result.success(null);
    } else if (call.method.equals("removeBadge")) {
      ShortcutBadger.removeCount(context);
      result.success(null);
    } else if (call.method.equals("isAppBadgeSupported")) {
      result.success(ShortcutBadger.isBadgeCounterSupported(context));
    } else {
      result.notImplemented();
    }
  }
}

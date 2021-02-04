// You have generated a new plugin project without
// specifying the `--platforms` flag. A plugin project supports no platforms is generated.
// To add platforms, run `flutter create -t plugin --platforms <platforms> .` under the same
// directory. You can also find a detailed instruction on how to add platforms in the `pubspec.yaml` at https://flutter.dev/docs/development/packages-and-plugins/developing-packages#plugin-platforms.

import 'dart:async';

import 'package:flutter/services.dart';

class AppCheck {
  static const MethodChannel _channel = const MethodChannel('app_check');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<List> get appList async {
    final List _appList = await _channel.invokeMethod('appCheck');
    return _appList;
  }

  static Future openAppWithPackageName(String packageName) async {
    await _channel.invokeMethod('openAppWithPackageName', packageName);
  }

  static Future<String> get topApp async {
    final String _topApp = await _channel.invokeMethod('getTopApp');
    return _topApp;
  }

  static Future checkPermission() async {
    await _channel.invokeMethod('checkPermission');
  }

  static Future<bool> startListen(List<String> _whiteList) async {
    return await _channel.invokeMethod('startListen', _whiteList);
  }

  static Future<bool> endListen() async {
    return await _channel.invokeMethod('endListen');
  }

  static void callbacks(Function callbackOn, Function callbackOff) {
    // ignore: missing_return
    _channel.setMethodCallHandler((call) {
      switch (call.method) {
        case "callbackOn":
          callbackOn();
          break;
        case "callbackOff":
          callbackOff();
          break;
        default:
          return null;
      }
    });
  }
}

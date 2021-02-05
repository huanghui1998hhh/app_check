import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:app_check/app_check.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  List _appList = [];
  String _topApp = "";

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    String platformVersion;
    try {
      platformVersion = await AppCheck.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    if (!mounted) return;

    _appList.clear();
    _appList = await AppCheck.appList;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: ListView(
          children: [
            InkWell(
                onTap: () async {
                  print(await AppCheck.appList);
                },
                child: Text('Running on: $_platformVersion\n')),
            Text(_appList.toString()),
            Text(_topApp ?? "null"),
            RaisedButton(onPressed: () async {
              await Future.delayed(Duration(seconds: 3), () async {
                print(await AppCheck.topApp);
              });
            }),
            SizedBox(height: 20),
            RaisedButton(onPressed: () async {
              await AppCheck.checkAppPermission();
            }),
            RaisedButton(onPressed: () async {
              AppCheck.callbacks(() {
                print("开");
              }, () {
                print("关");
              });
              await AppCheck.startListen(
                  ['gg.op.lol.android', 'com.miui.home']);
            }),
            RaisedButton(onPressed: () async {
              await AppCheck.endListen();
            })
          ],
        ),
      ),
    );
  }
}

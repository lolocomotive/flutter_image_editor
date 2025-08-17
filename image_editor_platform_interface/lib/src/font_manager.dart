import 'dart:io';

import 'channel.dart';

/// Wrapper for use channel to register font for add text.
class FontManager {
  const FontManager._();

  static Future<String> registerFont(File file, [String? fontName]) async {
    return await NativeChannel.channel.invokeMethod(
      'registerFont',
      <String, Object?>{'path': file.absolute.path,'name': fontName},
    );
  }
}

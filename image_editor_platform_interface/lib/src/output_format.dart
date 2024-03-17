import 'convert_value.dart';
import 'type.dart';

/// Used to describe the output format of a method.
class OutputFormat with JsonAble {
  /// jpeg format.
  const OutputFormat.jpeg(this.quality)
      : format = ImageFormat.jpeg,
        assert(quality > 0 && quality <= 100);

  /// png format.
  const OutputFormat.png([this.quality = 100])
      : format = ImageFormat.png,
        assert(quality > 0 && quality <= 100);

  /// webp format.
  const OutputFormat.webp([this.quality = 80])
      : format = ImageFormat.webp,
        assert(quality > 0 && quality <= 100);

  const OutputFormat.webp_lossy([this.quality = 80])
      : format = ImageFormat.webp_lossy,
        assert(quality > 0 && quality <= 100);
  const OutputFormat.webp_lossless([this.quality = 100])
      : format = ImageFormat.webp_lossless,
        assert(quality > 0 && quality <= 100);

  /// See [ImageFormat].
  final ImageFormat format;

  /// Range from 1 to 100.
  /// If format is png, then ios will ignore it.
  final int quality;

  @override
  Map<String, Object> toJson() {
    return <String, Object>{'format': format.index, 'quality': quality};
  }
}

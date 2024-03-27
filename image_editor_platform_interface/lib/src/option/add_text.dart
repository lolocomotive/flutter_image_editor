part of 'edit_options.dart';

/// Add text to image.
class AddTextOption implements Option {
  AddTextOption();

  /// The items of added texts.
  final List<EditorText> texts = <EditorText>[];

  /// Add [text] to [texts].
  void addText(EditorText text) {
    texts.add(text);
  }

  @override
  bool get canIgnore => texts.isEmpty;

  @override
  String get key => 'add_text';

  @override
  Map<String, Object> get transferValue {
    return <String, Object>{
      'texts': texts.map((e) => e.toJson()).toList(),
    };
  }
}

/// Descript of a text.
class EditorText {
  EditorText({
    required this.text,
    required this.transform,
    this.fontSize = 14,
    this.textColor = Colors.black,
    this.fontName = '',
  });

  /// The text.
  String text;

  /// The offset of text.
  Matrix4 transform;

  /// The font size of text.
  double fontSize;

  /// The color of text.
  Color textColor;

  /// The font name of text, if fontName is empty string, the text will use system font.
  String fontName;

  Map<String, Object> toJson() {
    return <String, Object>{
      'text': text,
      'fontName': fontName,
      'transform': transform.toMatrix3RowFirst(),
      'size': fontSize,
      'r': textColor.red,
      'g': textColor.green,
      'b': textColor.blue,
      'a': textColor.alpha,
    };
  }
}

extension MatrixConvert on Matrix4 {
  Matrix3 toMatrix3() {
    Float64List mat3 = Float64List(9);
    int offset = 0;
    for (int i = 0; i < 9; i++) {
      if ((i + offset) % 4 == 3) offset++;
      if (i + offset == 8) offset += 4;
      mat3[i] = storage[i + offset];
    }
    mat3[8] = 1;
    return Matrix3.fromList(mat3);
  }

  Float64List toMatrix3RowFirst() {
    print("Mat4: ");
    print(this);

    final mat3 = toMatrix3();
    print("Mat3: ");
    print(mat3);
    Float64List r = Float64List(9);
    for (int x = 0; x < 3; x++) {
      for (int y = 0; y < 3; y++) {
        r[3 * y + x] = mat3.storage[3 * x + y];
      }
    }
    print("Mat3 RowFirst: ");
    print(r);
    return r;
  }
}

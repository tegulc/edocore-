/// Le contrat d'API EDUCORE (Volume 6) impose le snake_case sur le fil.
/// Memes utilitaires que cote web (lib/caseConversion.ts) : le code Dart
/// reste en camelCase idiomatique, la conversion se fait aux frontieres.
dynamic versSnakeCase(dynamic valeur) {
  if (valeur is List) return valeur.map(versSnakeCase).toList();
  if (valeur is Map) {
    return valeur.map((cle, v) => MapEntry(_camelVersSnake(cle as String), versSnakeCase(v)));
  }
  return valeur;
}

dynamic versCamelCase(dynamic valeur) {
  if (valeur is List) return valeur.map(versCamelCase).toList();
  if (valeur is Map) {
    return valeur.map((cle, v) => MapEntry(_snakeVersCamel(cle as String), versCamelCase(v)));
  }
  return valeur;
}

String _camelVersSnake(String cle) {
  final buffer = StringBuffer();
  for (final rune in cle.runes) {
    final char = String.fromCharCode(rune);
    if (char.toUpperCase() == char && char.toLowerCase() != char) {
      buffer.write('_${char.toLowerCase()}');
    } else {
      buffer.write(char);
    }
  }
  return buffer.toString();
}

String _snakeVersCamel(String cle) {
  final parties = cle.split('_');
  return parties.first + parties.skip(1).map((p) => p.isEmpty ? '' : p[0].toUpperCase() + p.substring(1)).join();
}

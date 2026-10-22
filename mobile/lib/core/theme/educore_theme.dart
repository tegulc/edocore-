import 'package:flutter/material.dart';

/// Meme palette que le frontend web (tailwind.config.js) : bleu #2F5DE3, teal #0EA5A4.
class EducoreTheme {
  static const brand = Color(0xFF2F5DE3);
  static const accent = Color(0xFF0EA5A4);
  static const ink900 = Color(0xFF0B1220);

  static ThemeData clair() => ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: brand, secondary: accent, brightness: Brightness.light),
        fontFamily: 'Inter',
        appBarTheme: const AppBarTheme(backgroundColor: Colors.white, foregroundColor: ink900, elevation: 0),
      );

  static ThemeData sombre() => ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: brand, secondary: accent, brightness: Brightness.dark),
        fontFamily: 'Inter',
      );
}

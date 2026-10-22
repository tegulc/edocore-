#!/usr/bin/env python3
import bcrypt

# Générer un hash BCrypt pour le mot de passe "Temporaire123!"
password = "Temporaire123!"
salt = bcrypt.gensalt()
hashed = bcrypt.hashpw(password.encode('utf-8'), salt)
print(f"Mot de passe: {password}")
print(f"Hash BCrypt: {hashed.decode('utf-8')}")
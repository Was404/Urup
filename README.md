# Urup Android App

>[!NOTE] Создать безопасную клиент-серверную систему для хранения и обмена конфиденциальными документами с шифрованием на стороне клиента, многофакторной аутентификацией и аудитом доступа.

<<<<<<< HEAD
## Реализация шифрования по ГОСТ 2018

В Android требует использования сторонних библиотек, так как стандартные Android SDK не поддерживают этот алгоритм. 

- В данном проекте использовуется библиотека **Bouncy Castle**
- и ещё куча других 

### Доработки для реального приложения:

- Работа с хранилищем
  - Хранятся в директории: `/data/data/com.dk.urup/app_encrypted`
    (Доступно только приложению)

##### Генерация ключей:
В текущей реализации не сохраняются на устройстве. Генерируются "на лету" при каждом вызове:
```kotlin
val key = cryptoManager.generateKey("password", "salt".toByteArray())
```
##### Хранение ключей:

- [ ] Использовать Android KeyStore вместо хранения ключа
- [ ] Модифицировать "соль" : Хранить в `SharedPreferences` в шифрованном виде

### Авторизация

- [ ] регистрация не работает
- [ ] авторизация через гугл не работает
- [x] можно авторизоваться в случае если пользователь существует 

=======
>>>>>>> fcaa738 (Revert "fixed ANR +-")

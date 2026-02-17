# Test Documentation

This document describes the testing infrastructure for the MyMusic Android app.

## Test Structure

### Unit Tests (`app/src/test/`)
Unit tests run on the JVM without requiring an Android device or emulator. They are fast and ideal for testing business logic.

#### Available Test Classes:
1. **ThemeTypeTest** - Tests for the ThemeType enum
   - Validates all three theme types exist
   - Tests valueOf() functionality
   
2. **SongTest** - Tests for the Song data model
   - Tests object creation with all parameters
   - Validates default values (artist, album)
   - Tests album art URI generation
   
3. **ThemeHelperTest** - Tests for theme color logic
   - Validates color values for each theme
   - Tests theme names and descriptions
   - Validates gradient settings
   
4. **ExampleUnitTest** - Basic sanity test

### Instrumented Tests (`app/src/androidTest/`)
Instrumented tests run on an Android device or emulator. They test Android-specific functionality.

#### Available Test Classes:
1. **ThemeManagerTest** - Tests for theme persistence
   - Tests default theme (Spotify)
   - Tests saving and retrieving each theme
   - Tests persistence across ThemeManager instances
   - Tests theme overwriting
   
2. **ExampleInstrumentedTest** - Basic context test

## Running Tests

### Run All Unit Tests
```bash
./gradlew test
```

### Run Debug Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Run Release Unit Tests
```bash
./gradlew testReleaseUnitTest
```

### Run Specific Test Class
```bash
./gradlew test --tests ThemeHelperTest
./gradlew test --tests SongTest
```

### Run Instrumented Tests (requires device/emulator)
```bash
./gradlew connectedAndroidTest
```

### Run Specific Instrumented Test
```bash
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.mymusic.ThemeManagerTest
```

## Test Coverage

### Generate Coverage Report
```bash
./gradlew testDebugUnitTest jacocoTestReport
```

The coverage report will be generated at:
- HTML: `app/build/reports/jacoco/jacocoTestReport/html/index.html`
- XML: `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`

### View Coverage Report
```bash
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

## Continuous Integration

### GitHub Actions Workflow
The project includes a CI workflow (`.github/workflows/android-ci.yml`) that automatically:
- Builds the app on every push and pull request
- Runs all unit tests
- Executes lint checks
- Uploads test reports and build artifacts

### Workflow Triggers
- Push to `main`, `develop`, or `copilot/**` branches
- Pull requests to `main` or `develop` branches

### View CI Results
Check the "Actions" tab in the GitHub repository to see CI build results.

## Lint Checks

### Run Lint
```bash
./gradlew lint
```

### View Lint Report
```bash
open app/build/reports/lint-results-debug.html
```

## Test Coverage Summary

| Component | Unit Tests | Instrumented Tests |
|-----------|------------|-------------------|
| ThemeType | ✅ (5 tests) | - |
| Song | ✅ (5 tests) | - |
| ThemeHelper | ✅ (10 tests) | - |
| ThemeManager | - | ✅ (6 tests) |
| **Total** | **20 tests** | **6 tests** |

## Writing New Tests

### Unit Test Example
```kotlin
@Test
fun myTest_doesSomething() {
    // Arrange
    val input = "test"
    
    // Act
    val result = myFunction(input)
    
    // Assert
    assertEquals("expected", result)
}
```

### Instrumented Test Example
```kotlin
@RunWith(AndroidJUnit4::class)
class MyInstrumentedTest {
    
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }
    
    @Test
    fun myTest_usesContext() {
        // Test code using Android context
    }
}
```

## Dependencies

### Test Dependencies
- **JUnit 4** - Testing framework
- **Mockito** - Mocking framework
- **Robolectric** - Android unit testing
- **AndroidX Test** - Instrumented testing
- **Espresso** - UI testing
- **JaCoCo** - Code coverage

## Best Practices

1. **Test Naming**: Use descriptive names - `methodName_condition_expectedResult`
2. **Arrange-Act-Assert**: Structure tests clearly
3. **One Assertion**: Focus each test on a single behavior
4. **Fast Tests**: Keep unit tests fast by avoiding Android dependencies
5. **Clean Up**: Use `@Before` and `@After` for setup/teardown
6. **Coverage**: Aim for >80% code coverage on business logic

## Troubleshooting

### Tests Not Found
```bash
./gradlew clean test
```

### Out of Memory
Increase Gradle memory in `gradle.properties`:
```
org.gradle.jvmargs=-Xmx2048m
```

### Emulator Issues
Ensure an emulator is running:
```bash
adb devices
```

## References

- [Android Testing Guide](https://developer.android.com/training/testing)
- [JUnit 4 Documentation](https://junit.org/junit4/)
- [Mockito Documentation](https://site.mockito.org/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)

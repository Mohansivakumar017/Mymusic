#!/bin/bash
# MyMusic Test Runner Script
# This script helps run various test commands for the MyMusic app

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "======================================"
echo "   MyMusic Test Runner"
echo "======================================"
echo ""

# Function to print colored output
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    print_error "gradlew not found! Are you in the project root?"
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

# Parse command line arguments
COMMAND=${1:-help}

case $COMMAND in
    test|unit)
        print_info "Running unit tests..."
        ./gradlew testDebugUnitTest --stacktrace
        print_success "Unit tests completed!"
        ;;
        
    instrumented)
        print_info "Running instrumented tests (requires device/emulator)..."
        ./gradlew connectedAndroidTest --stacktrace
        print_success "Instrumented tests completed!"
        ;;
        
    all)
        print_info "Running all tests..."
        ./gradlew test connectedAndroidTest --stacktrace
        print_success "All tests completed!"
        ;;
        
    coverage)
        print_info "Generating test coverage report..."
        ./gradlew testDebugUnitTest jacocoTestReport --stacktrace
        print_success "Coverage report generated!"
        print_info "Report location: app/build/reports/jacoco/jacocoTestReport/html/index.html"
        ;;
        
    lint)
        print_info "Running lint checks..."
        ./gradlew lint --stacktrace
        print_success "Lint checks completed!"
        print_info "Report location: app/build/reports/lint-results-debug.html"
        ;;
        
    build)
        print_info "Building the app..."
        ./gradlew build --stacktrace
        print_success "Build completed!"
        ;;
        
    clean)
        print_info "Cleaning build artifacts..."
        ./gradlew clean
        print_success "Clean completed!"
        ;;
        
    check)
        print_info "Running all checks (build, test, lint)..."
        ./gradlew check --stacktrace
        print_success "All checks passed!"
        ;;
        
    help|*)
        echo "Usage: ./run-tests.sh [command]"
        echo ""
        echo "Commands:"
        echo "  test, unit     - Run unit tests"
        echo "  instrumented   - Run instrumented tests (requires device)"
        echo "  all            - Run all tests"
        echo "  coverage       - Generate test coverage report"
        echo "  lint           - Run lint checks"
        echo "  build          - Build the app"
        echo "  clean          - Clean build artifacts"
        echo "  check          - Run all checks (build, test, lint)"
        echo "  help           - Show this help message"
        echo ""
        echo "Examples:"
        echo "  ./run-tests.sh test       # Run unit tests"
        echo "  ./run-tests.sh coverage   # Generate coverage report"
        echo "  ./run-tests.sh check      # Run all checks"
        ;;
esac

echo ""
echo "======================================"

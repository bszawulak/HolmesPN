#include <iostream>
#include <vector>
#include <set>
#include <map>
#include <algorithm>
#include <numeric>
#include <queue>
#include "nlohmann/json.hpp" // Używamy biblioteki JSON

using namespace std;
using json = nlohmann::json;

// Funkcja transponująca macierz
vector<vector<int>> transposeMatrix(const vector<vector<int>>& matrix) {
    if (matrix.empty()) return {};

    size_t numRows = matrix.size();
    size_t numCols = matrix[0].size();

    vector<vector<int>> transposedMatrix(numCols, vector<int>(numRows));

    for (size_t i = 0; i < numRows; ++i) {
        for (size_t j = 0; j < numCols; ++j) {
            transposedMatrix[j][i] = matrix[i][j];
        }
    }

    return transposedMatrix;
}

// Funkcja walidująca stan (żadne miejsce nie ma ujemnych tokenów)
bool isValidState(const vector<int>& state) {
    for (int val : state) {
        if (val < 0) return false;
    }
    return true;
}

// Funkcja porównująca dwa oznaczenia
bool isMarkingEqual(const vector<int>& m1, const vector<int>& m2) {
    return m1 == m2;
}

// Funkcja oceny odległości od stanu początkowego
int distanceFromInitialState(const vector<int>& state, const vector<int>& m0) {
    int distance = 0;
    for (size_t i = 0; i < state.size(); ++i) {
        distance += abs(state[i] - m0[i]);
    }
    return distance;
}

// Funkcja realizująca metodę wsteczną z dynamicznym programowaniem
vector<vector<int>> shortest_path_backward(
    vector<int> m0, vector<int> md,
    const vector<vector<int>>& inputMatrix,
    const vector<vector<int>>& outputMatrix
) {
    size_t num_places = inputMatrix.size();
    size_t num_transitions = inputMatrix[0].size();

    map<vector<int>, int> visited; // Zapisujemy koszty dotarcia do stanów
    map<vector<int>, vector<int>> path; // Śledzimy rodziców stanów

    using StateCost = pair<int, vector<int>>;
    priority_queue<StateCost, vector<StateCost>, greater<>> pq;

    pq.push({ 0, md });
    visited[md] = 0;

    while (!pq.empty()) {
        int currentCost = pq.top().first;
        vector<int> currentState = pq.top().second;
        pq.pop();

        if (isMarkingEqual(currentState, m0)) break;

        for (size_t t = 0; t < num_transitions; ++t) {
            bool canFire = true;

            for (size_t p = 0; p < num_places; ++p) {
                if (currentState[p] < outputMatrix[p][t]) {
                    canFire = false;
                    break;
                }
            }

            if (canFire) {
                vector<int> newState = currentState;
                for (size_t p = 0; p < num_places; ++p) {
                    newState[p] = currentState[p] - outputMatrix[p][t] + inputMatrix[p][t];
                }

                int newCost = currentCost + 1;

                if (isValidState(newState) &&
                    (visited.find(newState) == visited.end() || newCost < visited[newState])) {
                    visited[newState] = newCost;
                    path[newState] = currentState;
                    pq.push({ newCost + distanceFromInitialState(newState, m0), newState });
                }
            }
        }
    }

    vector<vector<int>> resultPath;
    vector<int> state = m0;

    while (path.find(state) != path.end()) {
        resultPath.push_back(state);
        state = path[state];
    }

    resultPath.push_back(md);

    return resultPath;
}

// Główna funkcja programu
int main() {
    // Wczytaj dane wejściowe w formacie JSON
    string jsonInput;
    getline(cin, jsonInput);

    cout << "Received JSON Input:\n" << jsonInput << endl;

    vector<vector<int>> inputMatrix;
    vector<vector<int>> outputMatrix;
    vector<int> m0;
    vector<int> md;

    try {
        json inputJson = json::parse(jsonInput);

        // Odczytaj dane z JSON
        inputMatrix = inputJson["inputMatrix"].get<vector<vector<int>>>();
        outputMatrix = inputJson["outputMatrix"].get<vector<vector<int>>>();
        m0 = inputJson["startState"].get<vector<int>>();
        md = inputJson["endState"].get<vector<int>>();

        cout << "Parsed JSON successfully.\n";
    }
    catch (exception& e) {
        cerr << "Error parsing JSON: " << e.what() << endl;
        return 1; // Zakończ program z błędem
    }

    vector<vector<int>> InputMatrix = transposeMatrix(inputMatrix);
    vector<vector<int>> OutputMatrix = transposeMatrix(outputMatrix);

    // Przetwarzanie
    auto path = shortest_path_backward(m0, md, InputMatrix, OutputMatrix);

    // Przygotowanie wyniku w formacie JSON
    cout << "path:" << endl;
    for (const auto& state : path) {
        cout << "[";
        for (size_t i = 0; i < state.size(); ++i) {
            cout << state[i];
            if (i < state.size() - 1) {
                cout << ",";
            }
        }
        cout << "]" << endl;
    }

    return 0;
}
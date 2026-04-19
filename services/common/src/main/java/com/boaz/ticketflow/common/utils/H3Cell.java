package com.boaz.ticketflow.common.utils;

import java.io.IOException;
import java.util.List;

import com.uber.h3core.H3Core;

/**
 * Représente une cellule H3 sous forme de hexAddress (hexadécimal)
 * Fournit des utilitaires pour la conversion lat/lng → cellule, 
 * le calcul de voisins (k-ring), etc.
 */
public record H3Cell(String hexAddress) {

    // 🔹 Résolution par défaut pour les cellules H3
    // La résolution 9 correspond à environ 174m de côté de cellule
    //private static final int RESOLUTION = 9;

    // 🔹 Instance H3Core partagée pour toutes les méthodes statiques
    // Permet d'éviter de recréer H3Core à chaque appel
    private static H3Core h3;

    static {
        try {
            // 🔹 Initialisation de H3Core
            // Lance une RuntimeException si impossible
            h3 = H3Core.newInstance();
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'initialiser H3Core", e);
        }
    }

    // 🔹 Convertit latitude/longitude en hexAddress H3
    // Utilise la résolution par défaut
    public static String latLngToCell(Location location, int resolution) {
        return h3.latLngToCellAddress(location.latitude(), location.longitude(), resolution);
    }

    // 🔹 Crée un H3Cell à partir d'un objet Location et une résolution donnée
    // Retourne une nouvelle instance H3Cell
    public static H3Cell fromLocation(Location location, int resolution) {
        long cell = h3.latLngToCell(location.latitude(), location.longitude(), resolution);
        return new H3Cell(Long.toHexString(cell)); // Conversion en hexadécimal
    }

    // 🔹 Calcule les cellules voisines dans un rayon donné (k-ring)
    // center : cellule centrale
    // radius : nombre de "anneaux" autour de la cellule centrale
    // Retourne la liste des H3Cell dans le rayon
    public static List<H3Cell> kRing(H3Cell center, int radius) {
        // Convertit l'adresse hexadécimale en long
        long centerCell = Long.parseLong(center.hexAddress(), 16);

        // Récupère les cellules dans le rayon
        List<Long> cells = h3.gridDisk(centerCell, radius);

        // Convertit chaque long en hexadécimal et crée des H3Cell
        return cells.stream()
            .map(c -> new H3Cell(Long.toHexString(c)))
            .toList();
    }
}
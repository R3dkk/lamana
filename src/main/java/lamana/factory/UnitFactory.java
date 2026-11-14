package lamana.factory;

import lamana.model.animal.Animal;
import lamana.model.animal.melee.Ayam;
import lamana.model.animal.melee.Harimau;
import lamana.model.animal.melee.Godzilla;
import lamana.model.animal.ranged.Landak;
import lamana.model.animal.ranged.Monyet;
import lamana.model.animal.ranged.Naga;
import lamana.model.animal.heavy.KuraKura;
import lamana.model.animal.heavy.Badak;
import lamana.model.animal.heavy.Centaur;

import java.util.*;
import java.util.function.Supplier;

public final class UnitFactory {
    private UnitFactory() {}

    private static final Map<String, Supplier<Animal>> REGISTRY;
    static {
        Map<String, Supplier<Animal>> map = new LinkedHashMap<>();
        map.put("ayam", Ayam::new);
        map.put("landak", Landak::new);
        map.put("kurakura", KuraKura::new);
        map.put("kura", KuraKura::new);
        map.put("harimau", Harimau::new);
        map.put("monyet", Monyet::new);
        map.put("badak", Badak::new);
        map.put("godzilla", Godzilla::new);
        map.put("naga", Naga::new);
        map.put("centaur", Centaur::new);
        REGISTRY = Collections.unmodifiableMap(map);
    }

    public static Animal create(String type) {
        if (type == null) throw new IllegalArgumentException("Tipe hewan tidak boleh null.");
        Supplier<Animal> supplier = REGISTRY.get(type.toLowerCase());
        if (supplier == null) throw new IllegalArgumentException("Tipe hewan tidak dikenal: " + type);
        return supplier.get();
    }

    public static List<Animal> create(String type, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity harus > 0.");
        List<Animal> result = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) result.add(create(type));
        return result;
    }

    public static Set<String> getAvailableTypes() {
        return REGISTRY.keySet();
    }

    public static List<String> getTypesForLevel(int baseLevel) {
        List<String> result = new ArrayList<>();
        // Avoid duplicate aliases like "kura" vs "kurakura"
        Set<String> addedClasses = new HashSet<>();
        for (Map.Entry<String, Supplier<Animal>> entry : REGISTRY.entrySet()) {
            Animal sample = entry.getValue().get();
            String className = sample.getClass().getName();
            if (sample.getRequiredBaseLevel() == baseLevel && !addedClasses.contains(className)) {
                result.add(entry.getKey());
                addedClasses.add(className);
            }
        }
        return Collections.unmodifiableList(result);
    }
}

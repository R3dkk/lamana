package lamana;

import lamana.model.economy.MoneyExp;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pengujian unit untuk sistem ekonomi dan enkapsulasi MoneyExp.
 */
public class MoneyExpTest {

    @Test
    public void testInitialValues() {
        MoneyExp defaultRes = new MoneyExp();
        assertEquals(0, defaultRes.getMoney());
        assertEquals(0, defaultRes.getExp());

        MoneyExp customRes = new MoneyExp(120, 50);
        assertEquals(120, customRes.getMoney());
        assertEquals(50, customRes.getExp());
    }

    @Test
    public void testAddAndSpendMoney() {
        MoneyExp res = new MoneyExp(100, 0);

        res.addMoney(50);
        assertEquals(150, res.getMoney());

        // Pengurangan berhasil
        assertTrue(res.spendMoney(80));
        assertEquals(70, res.getMoney());

        // Pengurangan gagal jika saldo kurang (tidak mengubah nilai saldo)
        assertFalse(res.spendMoney(100));
        assertEquals(70, res.getMoney());
    }

    @Test
    public void testGainAndSpendExp() {
        MoneyExp res = new MoneyExp(0, 0);

        res.gainExp(150);
        assertEquals(150, res.getExp());

        assertTrue(res.spendExp(100));
        assertEquals(50, res.getExp());

        assertFalse(res.spendExp(80));
        assertEquals(50, res.getExp());
    }

    @Test
    public void testDefensiveValidation() {
        MoneyExp res = new MoneyExp();

        assertThrows(IllegalArgumentException.class, () -> res.addMoney(0));
        assertThrows(IllegalArgumentException.class, () -> res.addMoney(-10));
        assertThrows(IllegalArgumentException.class, () -> res.spendMoney(0));
        assertThrows(IllegalArgumentException.class, () -> res.spendMoney(-10));

        assertThrows(IllegalArgumentException.class, () -> res.gainExp(0));
        assertThrows(IllegalArgumentException.class, () -> res.gainExp(-10));
        assertThrows(IllegalArgumentException.class, () -> res.spendExp(0));
        assertThrows(IllegalArgumentException.class, () -> res.spendExp(-10));

        assertThrows(IllegalArgumentException.class, () -> new MoneyExp(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new MoneyExp(0, -1));
    }
}

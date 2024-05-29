import java.math.BigInteger;
import java.util.Random;
public class CurveP224 {

    public static void main(String[] args) throws Exception {
        BigInteger p = new BigInteger("ffffffffffffffffffffffffffffffff000000000000000000000001", 16);
        BigInteger a = new BigInteger("fffffffffffffffffffffffffffffffefffffffffffffffffffffffe", 16);
        BigInteger b = new BigInteger("b4050a850c04b3abf54132565044b0b7d7bfd8ba270b39432355ffb4", 16);
        BigInteger n = new BigInteger("ffffffffffffffffffffffffffff16a2e0b8f03e13dd29455c5c2a3d", 16);

        Random rand = new Random();
        int len = rand.nextInt(63) + 2;
        BigInteger res = new BigInteger(len, rand);
        //System.out.println("The random BigInteger = " + res);

        Curve E = new Curve(p,a,b, n);
        Curve.DotsGroup Base = E.new DotsGroup(new BigInteger("b70e0cbd6bb4bf7f321390b94a03c1d356c21122343280d6115c1d21", 16), new BigInteger("bd376388b5f723fb4c22dfe6cd4375a05a07476444d5819985007e34", 16), BigInteger.ONE);

        System.out.println("Hello, Diffie-Hellman protocol start. Alice generate da and calculate Qa");
        BigInteger da = new BigInteger("1", 10);
        while (da.equals(BigInteger.ONE) || da.equals(BigInteger.ZERO)) da = E.BlumBlumaBits(res, p);
        long t1 = System.currentTimeMillis();
        Curve.DotsGroup Qa = Base.ScalarMultipliacationMontgomery(Base, da);
        System.out.println("Qa: ");
        Qa.ToAffine(Qa).Pointsout();
        System.out.println("Bob generate db and calculate Qb");
        rand = new Random();
        len = rand.nextInt(63) + 2;
        res = new BigInteger(len, rand);
        BigInteger db = new BigInteger("1", 10);
        while (db.equals(BigInteger.ONE) || db.equals(BigInteger.ZERO)) db = E.BlumBlumaBits(res, p);
        Curve.DotsGroup Qb = Base.ScalarMultipliacationMontgomery(Base, db);
        System.out.println("Qb: ");
        Qb.ToAffine(Qb).Pointsout();
        System.out.println("Alice and Bob calculate shared secret");
        Curve.DotsGroup SA = Base.DiffieHellmanSecret(Qb, da);
        Curve.DotsGroup SB = Base.DiffieHellmanSecret(Qa, db);
        System.out.println("Check is it correct, that SA = SB");
        SA = SA.ToAffine(SA);
        SB = SB.ToAffine(SB);
        SA.Pointsout();
        SB.Pointsout();
        long t2 = System.currentTimeMillis();
        System.out.println("Times Diffie-Hellman protocol : " + (t2 - t1) + "mls");
        System.out.println("_____________________________________________________________________");
        System.out.println("/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
        t1 = System.currentTimeMillis();
        System.out.println("Hello, Directional encryption begin, Bob chose EC P-224 with base point P");
        Base.ToAffine(Base);
        System.out.println("Bob published his public key Qb");
        Qb.ToAffine(Qb);

        System.out.println("Start! Alice chose algorithm Enc and Wrap --- XOR :)");
        System.out.println("M = 1000110011");
        rand = new Random();
        len = rand.nextInt(63) + 2;
        res = new BigInteger(len, rand);
        BigInteger key = E.BlumBlumaBits(res, p.multiply(p)); //sym key
        String M = "1000110011";
        System.out.println("Alice generate C_M=Enc(M, k)");
        BigInteger Cm = Curve.Enc( M, key.toString(2));
        System.out.println(Cm.toString(16));
        System.out.println("Alice generate Shared secret S");
        rand = new Random();
        len = rand.nextInt(63) + 2;
        res = new BigInteger(len, rand);
        BigInteger ea = new BigInteger("1", 10);
        while (ea.equals(BigInteger.ONE) || ea.equals(BigInteger.ZERO)) ea = E.BlumBlumaBits(res, p);
        Curve.DotsGroup QA = Base.ScalarMultipliacationMontgomery(Base, ea);
        Curve.DotsGroup Sa = Qb.DiffieHellmanSecret(Qb, ea);
        Sa.Pointsout();
        BigInteger Sk = Curve.Wrap(key.toString(2), Sa); //incaps key
        System.out.println("Alice send her encrypted envelope");
        System.out.println("Qa = ");
        QA.Pointsout();
        System.out.println("Sa");
        Sa = Sa.ToAffine(Sa);
        Sa.Pointsout();
        System.out.println("Sk = " + Sk.toString(16));
        System.out.println("*********");
        System.out.println("Bob read Alice`s envelope");
        System.out.println("Calculate share secret");
        Curve.DotsGroup Sb = QA.DiffieHellmanSecret(QA, db);
        System.out.println("Sb = ");
        Sb = Sb.ToAffine(Sb);
        Sb.Pointsout();
        String keyBob = Curve.UnWrap(Sk, Sb);
        System.out.println("Decrypted M:");
        System.out.println(Curve.Dec(Cm,keyBob));
        t2 = System.currentTimeMillis();
        System.out.println("Times Directional encryption : " + (t2 - t1) + "mls");
        System.out.println("_____________________________________________________________________");

        System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
        System.out.println("Hello, Digital signature begin, Bob chose EC P-224 with base point P");
        System.out.println("Alice calculate H(M)");
        t1 = System.currentTimeMillis();
        BigInteger H = new BigInteger(Curve.SHA_512(M), 16);
        System.out.println(H.toString(16));
        rand = new Random();
        len = rand.nextInt(63) + 2;
        res = new BigInteger(len, rand);
        BigInteger k = new BigInteger("1", 10);
        while (k.equals(BigInteger.ONE) || k.equals(BigInteger.ZERO)) k = E.BlumBlumaBits(res, n);
        Curve.DotsGroup kP = Base.ScalarMultipliacationMontgomery(Base, k);
        kP = kP.ToAffine(kP);
        BigInteger r = kP.x.mod(n);
        while(r.equals(BigInteger.ZERO)) {
            rand = new Random();
            len = rand.nextInt(63) + 2;
            res = new BigInteger(len, rand);
            k = new BigInteger("1", 10);
            while (k.equals(BigInteger.ONE) || k.equals(BigInteger.ZERO)) k = E.BlumBlumaBits(res, n);
            kP = Base.ScalarMultipliacationMontgomery(Base, k);
            kP = kP.ToAffine(kP);
            r = kP.x.mod(n);
        }
        BigInteger s = Curve.Sign(k,H,da,r);
        System.out.println("Alice publish her signature");
        System.out.println("(r, s) = " + "("+ r.toString(16) + ", " + s.toString(16) + ")");

        System.out.println("**********");
        System.out.println("Lets check the signature");
        BigInteger Hb = new BigInteger(Curve.SHA_512(M), 16).mod(n);
        System.out.println("Bob sha");
        System.out.println(Hb.toString(16));
        Curve.Verify(r,s,Base,Qa,Hb);

        t2 = System.currentTimeMillis();
        System.out.println("Times Digital signature : " + (t2 - t1) + "mls");
        System.out.println("_____________________________________________________________________");
    }
}

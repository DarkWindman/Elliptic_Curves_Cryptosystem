import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Curve {
    private BigInteger p;
    private BigInteger a;
    private BigInteger b;
    private static BigInteger n;
    private static MessageDigest md;

    static {
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public Curve(BigInteger p, BigInteger a, BigInteger b, BigInteger n)
    {
        this.p = p;
        this.a = a;
        this.b = b;
        this.n = n;
    }
    static BigInteger BlumBlumaBits(BigInteger r1, BigInteger p) {
        String bits = p.toString(2);
        StringBuilder blbl = new StringBuilder("1");
        StringBuilder temp = new StringBuilder();
        BigInteger P = new BigInteger("D5BBB96D30086EC484EBA3D7F9CAEB07", 16);
        BigInteger Q = new BigInteger("425D2B9BFDB25B9CF6C416CC6E37B59C1F", 16);
        BigInteger n = P.multiply(Q);
        BigInteger r2;
        int i = 0;
        while (i == 0)
        {
            blbl = new StringBuilder();
            i = 1;
            while (blbl.length() != bits.length()) {
                r2 = r1.modPow(BigInteger.TWO, n);
                String last = r2.toString(2);
                temp.append(last.charAt(last.length() - 1));
                blbl.append(temp);
                temp = new StringBuilder();
                r1 = r2;
            }
            if(new BigInteger(blbl.toString(),2).compareTo(p) == -1) i = 1;
        }

        BigInteger result = new BigInteger(blbl.toString(), 2);
        return result;
    }

    public static String SHA_512(String input) {
        byte[] messageDigest = md.digest(input.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);
        return no.toString(16);
    }

    private static String xor(String a, String b) {
        StringBuilder res = new StringBuilder();
        if (a.length() != b.length()) {
            if (a.length() > b.length()) {
                for (int i = 0; i < a.length() - b.length(); i++) {
                    b = "0" + b;
                }
            } else {
                for (int i = 0; i < b.length() - a.length(); i++) {
                    a = "0" + a;
                }
            }
        }
        for (int i = 0; i < a.length(); i++) {
            if ((a.charAt(i) + b.charAt(i) - 96) == 0 || (a.charAt(i) + b.charAt(i) - 96) == 2) res.append(0);
            else res.append(1);
        }
        //System.out.println(res);
        return String.valueOf(res);
    }

    private static ArrayList<String> toonelenght(String a, String key)
    {
        ArrayList<String> newpair = new ArrayList<>();
        String block = key;
        while (a.length() != key.length()) {
            if(a.length() < key.length())  a = "0" + a;
            else {
                for (int i = 0; i < a.length()%key.length(); i++) key = key + block;
            }
        }
        newpair.add(a);
        newpair.add(key);
        return newpair;
    }

    public static BigInteger Enc(String M, String k)
    {
        ArrayList<String> pair = toonelenght(M, k);
        String xor = xor(pair.get(0), pair.get(1));
        BigInteger Cm = new BigInteger(xor, 2);
        return Cm;
    }

    public static BigInteger Wrap(String k, DotsGroup P)
    {
        String Sx = P.ToAffine(P).x.toString(2);
        ArrayList<String> pair = toonelenght(Sx, k);
        String xor = xor(pair.get(0), pair.get(1));
        BigInteger Ck = new BigInteger(xor, 2);
        return Ck;
    }

    public static String UnWrap(BigInteger Ck, DotsGroup P)
    {
        String Sx = P.ToAffine(P).x.toString(2);
        ArrayList<String> pair = toonelenght(Sx, Ck.toString(2));
        String xor = xor(pair.get(0), pair.get(1));
        return xor;
    }

    public static String Dec(BigInteger Cm, String k)
    {
        String xor = xor(Cm.toString(2), k);
        while(xor.charAt(0) == 48)
        {
            xor = xor.substring(1, xor.length());
        }
        return xor;
    }

    public static BigInteger Sign(BigInteger k, BigInteger H, BigInteger da, BigInteger r)
    {
        BigInteger s = k.modInverse(n).multiply(H.add(da.multiply(r)));
        return s;
    }

    public static void Verify(BigInteger r, BigInteger s,DotsGroup Base, DotsGroup Qa, BigInteger H)
    {
        BigInteger u1 = s.modInverse(n).multiply(H).mod(n);
        BigInteger u2 = s.modInverse(n).multiply(r).mod(n);
        Curve.DotsGroup u1P = Base.ScalarMultipliacationMontgomery(Base, u1);
        Curve.DotsGroup u2P = Base.ScalarMultipliacationMontgomery(Qa, u2);
        Curve.DotsGroup result = u1P.PointsAdd(u1P, u2P);
        result = result.ToAffine(result);
        result.Pointsout();
        BigInteger v = result.x.mod(n);
        if(v.equals(r)) System.out.println("Signature correct");
        else System.out.println("OOps, your signature is not correct");
    }
    private DotsGroup Inf = new DotsGroup(BigInteger.ZERO,BigInteger.ONE,BigInteger.ZERO);
    public class DotsGroup{
        public BigInteger x;
        public BigInteger y;
        public BigInteger z;
        public DotsGroup(BigInteger x, BigInteger y, BigInteger z)
        {
            /*if(z.equals(BigInteger.ONE)) {
                this.x = x;
                this.y = y;
            }
            else {
                this.x = x.multiply(z.modInverse(p)).mod(p);
                this.y = y.multiply(z.modInverse(p)).mod(p);
            }*/
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public void IsPoint (DotsGroup P)
        {
            BigInteger ysq= ((P.x.modPow(BigInteger.valueOf(3),p)).add(a.multiply(P.x))).add(b);
            ysq = ysq.mod(p);
            if(ysq.equals(P.y.modPow(BigInteger.TWO,p))) System.out.println("Consist");
            else System.out.println("OOPS... NOT Consist");
        }

        public DotsGroup PointDouble(DotsGroup P)
        {
            if(P == Inf) return Inf;
            else if(P.y.equals(BigInteger.ZERO)) return Inf;
            BigInteger W = a.multiply((P.z).modPow(BigInteger.TWO,p));
            W = W.add((P.x).modPow(BigInteger.TWO, p).multiply(BigInteger.valueOf(3)));
            BigInteger S = P.y.multiply(P.z).mod(p);
            BigInteger B = S.multiply(P.x.multiply(P.y)).mod(p);
            BigInteger H = W.modPow(BigInteger.TWO, p).subtract(B.multiply(BigInteger.valueOf(8))).mod(p);
            BigInteger x1 = BigInteger.TWO.multiply(H.multiply(S)).mod(p);
            BigInteger y1 = (W.multiply(((BigInteger.valueOf(4).multiply(B)).subtract(H)))).subtract((BigInteger.valueOf(8).multiply(P.y.modPow(BigInteger.TWO, p)).multiply(S.modPow(BigInteger.TWO, p))));
            y1 = y1.mod(p);
            BigInteger z1 = BigInteger.valueOf(8).multiply(S.modPow(BigInteger.valueOf(3), p)).mod(p);
            return new DotsGroup(x1,y1,z1);
        }

        public DotsGroup PointsAdd(DotsGroup P, DotsGroup Q)
        {
            if(P == Inf) return Q;
            else if(Q == Inf) return P;
            BigInteger u1 = Q.y.multiply(P.z).mod(p);
            BigInteger u2 = P.y.multiply(Q.z).mod(p);
            BigInteger v1 = Q.x.multiply(P.z).mod(p);
            BigInteger v2 = P.x.multiply(Q.z).mod(p);
            if(v1.equals(v2))
            {
                if(!u1.equals(u2)) return Inf;
                if(u1.equals(u2)) return PointDouble(P);
            }
            BigInteger u = u1.subtract(u2).mod(p);
            BigInteger v = v1.subtract(v2).mod(p);
            BigInteger w = P.z.multiply(Q.z);
            BigInteger A = ((u.modPow(BigInteger.TWO, p).multiply(w)).subtract(v.modPow(BigInteger.valueOf(3), p))).subtract(BigInteger.TWO.multiply(v.modPow(BigInteger.TWO, p)).multiply(v2)).mod(p);
            BigInteger x3 = v.multiply(A).mod(p);
            BigInteger y3 = u.multiply((v.modPow(BigInteger.TWO, p).multiply(v2)).subtract(A)).subtract(v.modPow(BigInteger.valueOf(3), p).multiply(u2)).mod(p);
            BigInteger z3 = v.modPow(BigInteger.valueOf(3), p).multiply(w).mod(p);
            return new DotsGroup(x3, y3, z3);
        }


        public DotsGroup ScalarMultipliacationMontgomery(DotsGroup P, BigInteger scalar)
        {
            DotsGroup r0 = Inf;
            DotsGroup r1 = P;
            String bitsline = scalar.toString(2);
            for (int i = 0; i < bitsline.length() ; i++)
            {
                if(bitsline.charAt(i) - 48 == 0)
                {
                    r1 = PointsAdd(r0,r1);
                    r0 = PointDouble(r0);
                }
                else {
                    r0 = PointsAdd(r0, r1);
                    r1 = PointDouble(r1);
                }
            }
            return r0;
        }

        public DotsGroup DiffieHellmanSecret(DotsGroup Qa, BigInteger db)
        {
            return ScalarMultipliacationMontgomery(Qa, db);
        }

        public DotsGroup ToAffine(DotsGroup P)
        {
            if(P == Inf) {
                System.out.println("P = Oe");
                return Inf;
            }
            else {
                //System.out.println(" (" + P.x.multiply(P.z.modInverse(p)).mod(p).toString(16) + ", " + P.y.multiply(P.z.modInverse(p)).mod(p).toString(16) + ")");
                DotsGroup aff = new DotsGroup(P.x.multiply(P.z.modInverse(p)).mod(p), P.y.multiply(P.z.modInverse(p)).mod(p), BigInteger.ONE);
                return aff;
            }
        }

        public void ToProjective(BigInteger x, BigInteger y, BigInteger z)
        {
            System.out.println(" (" + x.multiply(z.modInverse(p)).mod(p).toString(16) + ", " + y.multiply(z.modInverse(p)).mod(p).toString(16) + ", " + z + ")");
        }

        public void Pointsout()
        {
            System.out.println(" (" + x.toString(16) + ", " + y.toString(16) + ", " + z.toString(16) + ")");
        }
    }
}

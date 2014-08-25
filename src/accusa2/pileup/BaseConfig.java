package accusa2.pileup;

public class BaseConfig {

	// dictionary to convert byte to int -> index to ALL, or valid
	// TODO document
	static final int[] BYTE_BASE2INT_BASE = new int[86 + 1];
	static {
		for (int i = 0; i < BYTE_BASE2INT_BASE.length; ++i) {
			BYTE_BASE2INT_BASE[i] = -1;
		}
		BYTE_BASE2INT_BASE[65] = 0; // 65 A
		BYTE_BASE2INT_BASE[67] = 1; // 67 C
		BYTE_BASE2INT_BASE[71] = 2; // 71 G
		BYTE_BASE2INT_BASE[86] = 3; // 86 T
		BYTE_BASE2INT_BASE[78] = 4; // 78 N
	}

	public static final char STRAND_FORWARD_CHAR = '+';
	public static final char STRAND_REVERSE_CHAR = '-';
	public static final char STRAND_UNKNOWN_CHAR = '.';

	//									  0	   1	2	 3	  4
	public static final char[] ALL = { 'A', 'C', 'G', 'T', 'N' };
	public static final int[] INT_COMPLEMENTED = { 3, 2, 1, 0, 4 };

	// this bases are the one that are used for computation
	public static final char[] VALID = { 'A', 'C', 'G', 'T' };
	public static final char[] VALID_COMPLEMENTED = { 'T', 'G', 'C', 'A' };

	/*
	 * 
	 */

	private char[] bases;
	public int[] byte2int;
	
	public BaseConfig(char[] bases) {
		this.bases = bases;
		byte2int = Byte2baseI(bases);
	}

	// rearrange depending on given bases
	public int[] Byte2baseI(final char[] bases) {
		final int[] byte2int = new int[BYTE_BASE2INT_BASE.length];

		for (int i = 0; i < byte2int.length; ++i) {
			byte2int[i] = -1;
		}

		for (int i = 0; i < bases.length; ++i) {
			byte2int[(int)bases[i]] = i;
		}
		/*
		byte2int[65] = 0; // 65 A
		byte2int[67] = 1; // 67 C
		byte2int[71] = 2; // 71 G
		byte2int[86] = 3; // 86 T
		byte2int[78] = 4; // 78 N
		*/
		return byte2int;
	}

	public int getBaseI(byte base) {
		return byte2int[base];
	}

	public char[] getBases() {
		return bases;
	}

	public void setBases(char[] bases) {
		this.bases = bases;
		byte2int = Byte2baseI(bases);
	}
}
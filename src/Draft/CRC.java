/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Draft;

/**
 *
 * @author PC
 */
public class CRC
{
    public static final CRC CRC_16_CCITT =
        new CRC(16, 0x1021, 0xffff, 0xffff, true);
    public static final CRC CRC_32 =
        new CRC(32, 0x04c11db7, 0xffffffffL, 0xffffffffL, true);


    private final int _width;
    private final long _polynomial;
    private final long _mask;
    private final long _highBitMask;
    private final long _preset;
    private final long _postComplementMask;
    private final boolean _msbFirstBitOrder;
    private final int _shift;
    private final long[] _crcs;
/**
 * Constructs a CRC specification.
 *
 * @param width
 * @param polynomial
 * @param msbFirstBitOrder
 */
    public CRC(
        int width,
        long polynomial)
    {
        this(width, polynomial, 0, 0, true);
    }

/**
 * Constructs a CRC specification.
 *
 * @param width
 * @param polynomial
 * @param msbFirstBitOrder
 */
    public CRC(
        int width,
        long polynomial,
        long preset,
        long postComplementMask,
        boolean msbFirstBitOrder)
    {
        super();
        _width = width;
        _polynomial = polynomial;
        _mask = (1L << width) - 1;
        _highBitMask = (1L << (width - 1));
        _preset = preset;
        _postComplementMask = postComplementMask;
        _msbFirstBitOrder = msbFirstBitOrder;
        _shift = _width - 8;

        _crcs = new long[256];
        for (int i = 0; i < 256; i++)
        {
            _crcs[i] = crcForByte(i);
        }
    }


/**
 * Gets the width.
 *
 * @return  The width.
 */
    public int getWidth()
    {
        return _width;
    }


/**
 * Gets the polynomial.
 *
 * @return  The polynomial.
 */
    public long getPolynomial()
    {
        return _polynomial;
    }


/**
 * Gets the mask.
 *
 * @return  The mask.
 */
    public long getMask()
    {
        return _mask;
    }


/**
 * Gets the preset.
 *
 * @return  The preset.
 */
    public long getPreset()
    {
        return _preset;
    }


/**
 * Gets the post-complement mask.
 *
 * @return  The post-complement mask.
 */
    public long getPostComplementMask()
    {
        return _postComplementMask;
    }


/**
 * @return  True if this CRC uses MSB first bit order.
 */
    public boolean isMsbFirstBitOrder()
    {
        return _msbFirstBitOrder;
    }


    public long computeBitwise(byte[] message)
    {
        long result = _preset;

        for (int i = 0; i < message.length; i++)
        {
            for (int j = 0; j < 8; j++)
            {
                final int bitIndex = _msbFirstBitOrder ? 7 - j : j;
                final boolean messageBit = (message[i] & (1 << bitIndex)) != 0;
                final boolean crcBit = (result & _highBitMask) != 0;

                result <<= 1;
                if (messageBit ^ crcBit)
                {
                    result ^= _polynomial;
                }
                result &= _mask;
            }
        }

        return result ^ _postComplementMask;
    }


    public long compute(byte[] message)
    {
        long result = _preset;

        for (int i = 0; i < message.length; i++)
        {
            final int b = (int) (message[i] ^ (result >>> _shift)) & 0xff;

            result = ((result << 8) ^ _crcs[b]) & _mask;
        }
        return result ^ _postComplementMask;
    }


    private long crcForByte(int b)
    {
        long result1 = (b & 0xff) << _shift;
        for (int j = 0; j < 8; j++)
        {
            final boolean crcBit = (result1 & (1L << (_width - 1))) != 0;

            result1 <<= 1;
            if (crcBit)
            {
                result1 ^= _polynomial;
            }
            result1 &= _mask;
        }
        return result1;
    }


    public String crcTable()
    {
        final int digits = (_width + 3) / 4;
        final int itemsPerLine = (digits + 4) * 8 < 72 ? 8 : 4;

        final String format = "0x%0" + digits + "x, ";

        final StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        for (int i = 0; i < _crcs.length; i += itemsPerLine)
        {
            builder.append("    ");
            for (int j = i; j < i + itemsPerLine; j++)
            {
                builder.append(String.format(format, _crcs[j]));
            }
            builder.append("\n");
        }
        builder.append("}\n");
        return builder.toString();
    }
}

begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|KeyValue
operator|.
name|Type
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|ByteBufferUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|primitives
operator|.
name|Longs
import|;
end_import

begin_comment
comment|/**  * Compare two HBase cells.  Do not use this method comparing<code>-ROOT-</code> or  *<code>hbase:meta</code> cells.  Cells from these tables need a specialized comparator, one that  * takes account of the special formatting of the row where we have commas to delimit table from  * regionname, from row.  See KeyValue for how it has a special comparator to do hbase:meta cells  * and yet another for -ROOT-.  *<p>While using this comparator for {{@link #compareRows(Cell, Cell)} et al, the hbase:meta cells  * format should be taken into consideration, for which the instance of this comparator  * should be used.  In all other cases the static APIs in this comparator would be enough  *<p>HOT methods. We spend a good portion of CPU comparing. Anything that makes the compare  * faster will likely manifest at the macro level. See also  * {@link BBKVComparator}. Use it when mostly {@link ByteBufferKeyValue}s.  *</p>  */
end_comment

begin_class
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"UNKNOWN"
argument_list|,
name|justification
operator|=
literal|"Findbugs doesn't like the way we are negating the result of a compare in below"
argument_list|)
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|CellComparatorImpl
implements|implements
name|CellComparator
block|{
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|CellComparatorImpl
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Comparator for plain key/values; i.e. non-catalog table key/values. Works on Key portion    * of KeyValue only.    */
specifier|public
specifier|static
specifier|final
name|CellComparatorImpl
name|COMPARATOR
init|=
operator|new
name|CellComparatorImpl
argument_list|()
decl_stmt|;
comment|/**    * A {@link CellComparatorImpl} for<code>hbase:meta</code> catalog table    * {@link KeyValue}s.    */
specifier|public
specifier|static
specifier|final
name|CellComparatorImpl
name|META_COMPARATOR
init|=
operator|new
name|MetaCellComparator
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
specifier|final
name|int
name|compare
parameter_list|(
specifier|final
name|Cell
name|a
parameter_list|,
specifier|final
name|Cell
name|b
parameter_list|)
block|{
return|return
name|compare
argument_list|(
name|a
argument_list|,
name|b
argument_list|,
literal|false
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
specifier|final
name|Cell
name|a
parameter_list|,
specifier|final
name|Cell
name|b
parameter_list|,
name|boolean
name|ignoreSequenceid
parameter_list|)
block|{
name|int
name|diff
init|=
literal|0
decl_stmt|;
comment|// "Peel off" the most common path.
if|if
condition|(
name|a
operator|instanceof
name|ByteBufferKeyValue
operator|&&
name|b
operator|instanceof
name|ByteBufferKeyValue
condition|)
block|{
name|diff
operator|=
name|BBKVComparator
operator|.
name|compare
argument_list|(
operator|(
name|ByteBufferKeyValue
operator|)
name|a
argument_list|,
operator|(
name|ByteBufferKeyValue
operator|)
name|b
argument_list|,
name|ignoreSequenceid
argument_list|)
expr_stmt|;
if|if
condition|(
name|diff
operator|!=
literal|0
condition|)
block|{
return|return
name|diff
return|;
block|}
block|}
else|else
block|{
name|diff
operator|=
name|compareRows
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
expr_stmt|;
if|if
condition|(
name|diff
operator|!=
literal|0
condition|)
block|{
return|return
name|diff
return|;
block|}
name|diff
operator|=
name|compareWithoutRow
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
expr_stmt|;
if|if
condition|(
name|diff
operator|!=
literal|0
condition|)
block|{
return|return
name|diff
return|;
block|}
block|}
comment|// Negate following comparisons so later edits show up first mvccVersion: later sorts first
return|return
name|ignoreSequenceid
condition|?
name|diff
else|:
name|Long
operator|.
name|compare
argument_list|(
name|b
operator|.
name|getSequenceId
argument_list|()
argument_list|,
name|a
operator|.
name|getSequenceId
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Compares the family and qualifier part of the cell    * @return 0 if both cells are equal, 1 if left cell is bigger than right, -1 otherwise    */
specifier|public
specifier|final
name|int
name|compareColumns
parameter_list|(
specifier|final
name|Cell
name|left
parameter_list|,
specifier|final
name|Cell
name|right
parameter_list|)
block|{
name|int
name|diff
init|=
name|compareFamilies
argument_list|(
name|left
argument_list|,
name|right
argument_list|)
decl_stmt|;
if|if
condition|(
name|diff
operator|!=
literal|0
condition|)
block|{
return|return
name|diff
return|;
block|}
return|return
name|compareQualifiers
argument_list|(
name|left
argument_list|,
name|right
argument_list|)
return|;
block|}
comment|/**    * Compare the families of left and right cell    * @return 0 if both cells are equal, 1 if left cell is bigger than right, -1 otherwise    */
annotation|@
name|Override
specifier|public
specifier|final
name|int
name|compareFamilies
parameter_list|(
name|Cell
name|left
parameter_list|,
name|Cell
name|right
parameter_list|)
block|{
if|if
condition|(
name|left
operator|instanceof
name|ByteBufferExtendedCell
operator|&&
name|right
operator|instanceof
name|ByteBufferExtendedCell
condition|)
block|{
return|return
name|ByteBufferUtils
operator|.
name|compareTo
argument_list|(
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|left
operator|)
operator|.
name|getFamilyByteBuffer
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|left
operator|)
operator|.
name|getFamilyPosition
argument_list|()
argument_list|,
name|left
operator|.
name|getFamilyLength
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|right
operator|)
operator|.
name|getFamilyByteBuffer
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|right
operator|)
operator|.
name|getFamilyPosition
argument_list|()
argument_list|,
name|right
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
return|;
block|}
if|if
condition|(
name|left
operator|instanceof
name|ByteBufferExtendedCell
condition|)
block|{
return|return
name|ByteBufferUtils
operator|.
name|compareTo
argument_list|(
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|left
operator|)
operator|.
name|getFamilyByteBuffer
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|left
operator|)
operator|.
name|getFamilyPosition
argument_list|()
argument_list|,
name|left
operator|.
name|getFamilyLength
argument_list|()
argument_list|,
name|right
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|right
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|right
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
return|;
block|}
if|if
condition|(
name|right
operator|instanceof
name|ByteBufferExtendedCell
condition|)
block|{
comment|// Notice how we flip the order of the compare here. We used to negate the return value but
comment|// see what FindBugs says
comment|// http://findbugs.sourceforge.net/bugDescriptions.html#RV_NEGATING_RESULT_OF_COMPARETO
comment|// It suggest flipping the order to get same effect and 'safer'.
return|return
name|ByteBufferUtils
operator|.
name|compareTo
argument_list|(
name|left
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|left
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|left
operator|.
name|getFamilyLength
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|right
operator|)
operator|.
name|getFamilyByteBuffer
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|right
operator|)
operator|.
name|getFamilyPosition
argument_list|()
argument_list|,
name|right
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
return|;
block|}
return|return
name|Bytes
operator|.
name|compareTo
argument_list|(
name|left
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|left
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|left
operator|.
name|getFamilyLength
argument_list|()
argument_list|,
name|right
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|right
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|right
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Compare the qualifiers part of the left and right cells.    * @return 0 if both cells are equal, 1 if left cell is bigger than right, -1 otherwise    */
annotation|@
name|Override
specifier|public
specifier|final
name|int
name|compareQualifiers
parameter_list|(
name|Cell
name|left
parameter_list|,
name|Cell
name|right
parameter_list|)
block|{
if|if
condition|(
name|left
operator|instanceof
name|ByteBufferExtendedCell
operator|&&
name|right
operator|instanceof
name|ByteBufferExtendedCell
condition|)
block|{
return|return
name|ByteBufferUtils
operator|.
name|compareTo
argument_list|(
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|left
operator|)
operator|.
name|getQualifierByteBuffer
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|left
operator|)
operator|.
name|getQualifierPosition
argument_list|()
argument_list|,
name|left
operator|.
name|getQualifierLength
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|right
operator|)
operator|.
name|getQualifierByteBuffer
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|right
operator|)
operator|.
name|getQualifierPosition
argument_list|()
argument_list|,
name|right
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
return|;
block|}
if|if
condition|(
name|left
operator|instanceof
name|ByteBufferExtendedCell
condition|)
block|{
return|return
name|ByteBufferUtils
operator|.
name|compareTo
argument_list|(
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|left
operator|)
operator|.
name|getQualifierByteBuffer
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|left
operator|)
operator|.
name|getQualifierPosition
argument_list|()
argument_list|,
name|left
operator|.
name|getQualifierLength
argument_list|()
argument_list|,
name|right
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|right
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|right
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
return|;
block|}
if|if
condition|(
name|right
operator|instanceof
name|ByteBufferExtendedCell
condition|)
block|{
comment|// Notice how we flip the order of the compare here. We used to negate the return value but
comment|// see what FindBugs says
comment|// http://findbugs.sourceforge.net/bugDescriptions.html#RV_NEGATING_RESULT_OF_COMPARETO
comment|// It suggest flipping the order to get same effect and 'safer'.
return|return
name|ByteBufferUtils
operator|.
name|compareTo
argument_list|(
name|left
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|left
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|left
operator|.
name|getQualifierLength
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|right
operator|)
operator|.
name|getQualifierByteBuffer
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|right
operator|)
operator|.
name|getQualifierPosition
argument_list|()
argument_list|,
name|right
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
return|;
block|}
return|return
name|Bytes
operator|.
name|compareTo
argument_list|(
name|left
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|left
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|left
operator|.
name|getQualifierLength
argument_list|()
argument_list|,
name|right
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|right
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|right
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Compares the rows of the left and right cell.    * For the hbase:meta case this method is overridden such that it can handle hbase:meta cells.    * The caller should ensure using the appropriate comparator for hbase:meta.    * @return 0 if both cells are equal, 1 if left cell is bigger than right, -1 otherwise    */
annotation|@
name|Override
specifier|public
name|int
name|compareRows
parameter_list|(
specifier|final
name|Cell
name|left
parameter_list|,
specifier|final
name|Cell
name|right
parameter_list|)
block|{
return|return
name|compareRows
argument_list|(
name|left
argument_list|,
name|left
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|right
argument_list|,
name|right
operator|.
name|getRowLength
argument_list|()
argument_list|)
return|;
block|}
specifier|static
name|int
name|compareRows
parameter_list|(
specifier|final
name|Cell
name|left
parameter_list|,
name|int
name|leftRowLength
parameter_list|,
specifier|final
name|Cell
name|right
parameter_list|,
name|int
name|rightRowLength
parameter_list|)
block|{
comment|// left and right can be exactly the same at the beginning of a row
if|if
condition|(
name|left
operator|==
name|right
condition|)
block|{
return|return
literal|0
return|;
block|}
if|if
condition|(
name|left
operator|instanceof
name|ByteBufferExtendedCell
operator|&&
name|right
operator|instanceof
name|ByteBufferExtendedCell
condition|)
block|{
return|return
name|ByteBufferUtils
operator|.
name|compareTo
argument_list|(
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|left
operator|)
operator|.
name|getRowByteBuffer
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|left
operator|)
operator|.
name|getRowPosition
argument_list|()
argument_list|,
name|leftRowLength
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|right
operator|)
operator|.
name|getRowByteBuffer
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|right
operator|)
operator|.
name|getRowPosition
argument_list|()
argument_list|,
name|rightRowLength
argument_list|)
return|;
block|}
if|if
condition|(
name|left
operator|instanceof
name|ByteBufferExtendedCell
condition|)
block|{
return|return
name|ByteBufferUtils
operator|.
name|compareTo
argument_list|(
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|left
operator|)
operator|.
name|getRowByteBuffer
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|left
operator|)
operator|.
name|getRowPosition
argument_list|()
argument_list|,
name|leftRowLength
argument_list|,
name|right
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|right
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|rightRowLength
argument_list|)
return|;
block|}
if|if
condition|(
name|right
operator|instanceof
name|ByteBufferExtendedCell
condition|)
block|{
comment|// Notice how we flip the order of the compare here. We used to negate the return value but
comment|// see what FindBugs says
comment|// http://findbugs.sourceforge.net/bugDescriptions.html#RV_NEGATING_RESULT_OF_COMPARETO
comment|// It suggest flipping the order to get same effect and 'safer'.
return|return
name|ByteBufferUtils
operator|.
name|compareTo
argument_list|(
name|left
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|left
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|leftRowLength
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|right
operator|)
operator|.
name|getRowByteBuffer
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|right
operator|)
operator|.
name|getRowPosition
argument_list|()
argument_list|,
name|rightRowLength
argument_list|)
return|;
block|}
return|return
name|Bytes
operator|.
name|compareTo
argument_list|(
name|left
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|left
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|left
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|right
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|right
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|right
operator|.
name|getRowLength
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Compares the row part of the cell with a simple plain byte[] like the    * stopRow in Scan. This should be used with context where for hbase:meta    * cells the {{@link #META_COMPARATOR} should be used    *    * @param left    *          the cell to be compared    * @param right    *          the kv serialized byte[] to be compared with    * @param roffset    *          the offset in the byte[]    * @param rlength    *          the length in the byte[]    * @return 0 if both cell and the byte[] are equal, 1 if the cell is bigger    *         than byte[], -1 otherwise    */
annotation|@
name|Override
specifier|public
name|int
name|compareRows
parameter_list|(
name|Cell
name|left
parameter_list|,
name|byte
index|[]
name|right
parameter_list|,
name|int
name|roffset
parameter_list|,
name|int
name|rlength
parameter_list|)
block|{
if|if
condition|(
name|left
operator|instanceof
name|ByteBufferExtendedCell
condition|)
block|{
return|return
name|ByteBufferUtils
operator|.
name|compareTo
argument_list|(
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|left
operator|)
operator|.
name|getRowByteBuffer
argument_list|()
argument_list|,
operator|(
operator|(
name|ByteBufferExtendedCell
operator|)
name|left
operator|)
operator|.
name|getRowPosition
argument_list|()
argument_list|,
name|left
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|right
argument_list|,
name|roffset
argument_list|,
name|rlength
argument_list|)
return|;
block|}
return|return
name|Bytes
operator|.
name|compareTo
argument_list|(
name|left
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|left
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|left
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|right
argument_list|,
name|roffset
argument_list|,
name|rlength
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|int
name|compareWithoutRow
parameter_list|(
specifier|final
name|Cell
name|left
parameter_list|,
specifier|final
name|Cell
name|right
parameter_list|)
block|{
comment|// If the column is not specified, the "minimum" key type appears the
comment|// latest in the sorted order, regardless of the timestamp. This is used
comment|// for specifying the last key/value in a given row, because there is no
comment|// "lexicographically last column" (it would be infinitely long). The
comment|// "maximum" key type does not need this behavior.
comment|// Copied from KeyValue. This is bad in that we can't do memcmp w/ special rules like this.
name|int
name|lFamLength
init|=
name|left
operator|.
name|getFamilyLength
argument_list|()
decl_stmt|;
name|int
name|rFamLength
init|=
name|right
operator|.
name|getFamilyLength
argument_list|()
decl_stmt|;
name|int
name|lQualLength
init|=
name|left
operator|.
name|getQualifierLength
argument_list|()
decl_stmt|;
name|int
name|rQualLength
init|=
name|right
operator|.
name|getQualifierLength
argument_list|()
decl_stmt|;
if|if
condition|(
name|lFamLength
operator|+
name|lQualLength
operator|==
literal|0
operator|&&
name|left
operator|.
name|getTypeByte
argument_list|()
operator|==
name|Type
operator|.
name|Minimum
operator|.
name|getCode
argument_list|()
condition|)
block|{
comment|// left is "bigger", i.e. it appears later in the sorted order
return|return
literal|1
return|;
block|}
if|if
condition|(
name|rFamLength
operator|+
name|rQualLength
operator|==
literal|0
operator|&&
name|right
operator|.
name|getTypeByte
argument_list|()
operator|==
name|Type
operator|.
name|Minimum
operator|.
name|getCode
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|lFamLength
operator|!=
name|rFamLength
condition|)
block|{
comment|// comparing column family is enough.
return|return
name|compareFamilies
argument_list|(
name|left
argument_list|,
name|right
argument_list|)
return|;
block|}
comment|// Compare cf:qualifier
name|int
name|diff
init|=
name|compareColumns
argument_list|(
name|left
argument_list|,
name|right
argument_list|)
decl_stmt|;
if|if
condition|(
name|diff
operator|!=
literal|0
condition|)
block|{
return|return
name|diff
return|;
block|}
name|diff
operator|=
name|compareTimestamps
argument_list|(
name|left
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|right
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|diff
operator|!=
literal|0
condition|)
block|{
return|return
name|diff
return|;
block|}
comment|// Compare types. Let the delete types sort ahead of puts; i.e. types
comment|// of higher numbers sort before those of lesser numbers. Maximum (255)
comment|// appears ahead of everything, and minimum (0) appears after
comment|// everything.
return|return
operator|(
literal|0xff
operator|&
name|right
operator|.
name|getTypeByte
argument_list|()
operator|)
operator|-
operator|(
literal|0xff
operator|&
name|left
operator|.
name|getTypeByte
argument_list|()
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTimestamps
parameter_list|(
specifier|final
name|Cell
name|left
parameter_list|,
specifier|final
name|Cell
name|right
parameter_list|)
block|{
return|return
name|compareTimestamps
argument_list|(
name|left
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|right
operator|.
name|getTimestamp
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTimestamps
parameter_list|(
specifier|final
name|long
name|ltimestamp
parameter_list|,
specifier|final
name|long
name|rtimestamp
parameter_list|)
block|{
comment|// Swap order we pass into compare so we get DESCENDING order.
return|return
name|Long
operator|.
name|compare
argument_list|(
name|rtimestamp
argument_list|,
name|ltimestamp
argument_list|)
return|;
block|}
comment|/**    * A {@link CellComparatorImpl} for<code>hbase:meta</code> catalog table    * {@link KeyValue}s.    */
specifier|public
specifier|static
class|class
name|MetaCellComparator
extends|extends
name|CellComparatorImpl
block|{
comment|// TODO: Do we need a ByteBufferKeyValue version of this?
annotation|@
name|Override
specifier|public
name|int
name|compareRows
parameter_list|(
specifier|final
name|Cell
name|left
parameter_list|,
specifier|final
name|Cell
name|right
parameter_list|)
block|{
return|return
name|compareRows
argument_list|(
name|left
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|left
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|left
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|right
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|right
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|right
operator|.
name|getRowLength
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareRows
parameter_list|(
name|Cell
name|left
parameter_list|,
name|byte
index|[]
name|right
parameter_list|,
name|int
name|roffset
parameter_list|,
name|int
name|rlength
parameter_list|)
block|{
return|return
name|compareRows
argument_list|(
name|left
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|left
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|left
operator|.
name|getRowLength
argument_list|()
argument_list|,
name|right
argument_list|,
name|roffset
argument_list|,
name|rlength
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
specifier|final
name|Cell
name|a
parameter_list|,
specifier|final
name|Cell
name|b
parameter_list|,
name|boolean
name|ignoreSequenceid
parameter_list|)
block|{
name|int
name|diff
init|=
name|compareRows
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
decl_stmt|;
if|if
condition|(
name|diff
operator|!=
literal|0
condition|)
block|{
return|return
name|diff
return|;
block|}
name|diff
operator|=
name|compareWithoutRow
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
expr_stmt|;
if|if
condition|(
name|diff
operator|!=
literal|0
condition|)
block|{
return|return
name|diff
return|;
block|}
comment|// Negate following comparisons so later edits show up first mvccVersion: later sorts first
return|return
name|ignoreSequenceid
condition|?
name|diff
else|:
name|Longs
operator|.
name|compare
argument_list|(
name|b
operator|.
name|getSequenceId
argument_list|()
argument_list|,
name|a
operator|.
name|getSequenceId
argument_list|()
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|int
name|compareRows
parameter_list|(
name|byte
index|[]
name|left
parameter_list|,
name|int
name|loffset
parameter_list|,
name|int
name|llength
parameter_list|,
name|byte
index|[]
name|right
parameter_list|,
name|int
name|roffset
parameter_list|,
name|int
name|rlength
parameter_list|)
block|{
name|int
name|leftDelimiter
init|=
name|Bytes
operator|.
name|searchDelimiterIndex
argument_list|(
name|left
argument_list|,
name|loffset
argument_list|,
name|llength
argument_list|,
name|HConstants
operator|.
name|DELIMITER
argument_list|)
decl_stmt|;
name|int
name|rightDelimiter
init|=
name|Bytes
operator|.
name|searchDelimiterIndex
argument_list|(
name|right
argument_list|,
name|roffset
argument_list|,
name|rlength
argument_list|,
name|HConstants
operator|.
name|DELIMITER
argument_list|)
decl_stmt|;
comment|// Compare up to the delimiter
name|int
name|lpart
init|=
operator|(
name|leftDelimiter
operator|<
literal|0
condition|?
name|llength
else|:
name|leftDelimiter
operator|-
name|loffset
operator|)
decl_stmt|;
name|int
name|rpart
init|=
operator|(
name|rightDelimiter
operator|<
literal|0
condition|?
name|rlength
else|:
name|rightDelimiter
operator|-
name|roffset
operator|)
decl_stmt|;
name|int
name|result
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|left
argument_list|,
name|loffset
argument_list|,
name|lpart
argument_list|,
name|right
argument_list|,
name|roffset
argument_list|,
name|rpart
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
block|{
return|return
name|result
return|;
block|}
else|else
block|{
if|if
condition|(
name|leftDelimiter
operator|<
literal|0
operator|&&
name|rightDelimiter
operator|>=
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|rightDelimiter
operator|<
literal|0
operator|&&
name|leftDelimiter
operator|>=
literal|0
condition|)
block|{
return|return
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|leftDelimiter
operator|<
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
block|}
comment|// Compare middle bit of the row.
comment|// Move past delimiter
name|leftDelimiter
operator|++
expr_stmt|;
name|rightDelimiter
operator|++
expr_stmt|;
name|int
name|leftFarDelimiter
init|=
name|Bytes
operator|.
name|searchDelimiterIndexInReverse
argument_list|(
name|left
argument_list|,
name|leftDelimiter
argument_list|,
name|llength
operator|-
operator|(
name|leftDelimiter
operator|-
name|loffset
operator|)
argument_list|,
name|HConstants
operator|.
name|DELIMITER
argument_list|)
decl_stmt|;
name|int
name|rightFarDelimiter
init|=
name|Bytes
operator|.
name|searchDelimiterIndexInReverse
argument_list|(
name|right
argument_list|,
name|rightDelimiter
argument_list|,
name|rlength
operator|-
operator|(
name|rightDelimiter
operator|-
name|roffset
operator|)
argument_list|,
name|HConstants
operator|.
name|DELIMITER
argument_list|)
decl_stmt|;
comment|// Now compare middlesection of row.
name|lpart
operator|=
operator|(
name|leftFarDelimiter
operator|<
literal|0
condition|?
name|llength
operator|+
name|loffset
else|:
name|leftFarDelimiter
operator|)
operator|-
name|leftDelimiter
expr_stmt|;
name|rpart
operator|=
operator|(
name|rightFarDelimiter
operator|<
literal|0
condition|?
name|rlength
operator|+
name|roffset
else|:
name|rightFarDelimiter
operator|)
operator|-
name|rightDelimiter
expr_stmt|;
name|result
operator|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|left
argument_list|,
name|leftDelimiter
argument_list|,
name|lpart
argument_list|,
name|right
argument_list|,
name|rightDelimiter
argument_list|,
name|rpart
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
block|{
return|return
name|result
return|;
block|}
else|else
block|{
if|if
condition|(
name|leftDelimiter
operator|<
literal|0
operator|&&
name|rightDelimiter
operator|>=
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|rightDelimiter
operator|<
literal|0
operator|&&
name|leftDelimiter
operator|>=
literal|0
condition|)
block|{
return|return
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|leftDelimiter
operator|<
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
block|}
comment|// Compare last part of row, the rowid.
name|leftFarDelimiter
operator|++
expr_stmt|;
name|rightFarDelimiter
operator|++
expr_stmt|;
name|result
operator|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|left
argument_list|,
name|leftFarDelimiter
argument_list|,
name|llength
operator|-
operator|(
name|leftFarDelimiter
operator|-
name|loffset
operator|)
argument_list|,
name|right
argument_list|,
name|rightFarDelimiter
argument_list|,
name|rlength
operator|-
operator|(
name|rightFarDelimiter
operator|-
name|roffset
operator|)
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareRows
parameter_list|(
name|ByteBuffer
name|row
parameter_list|,
name|Cell
name|cell
parameter_list|)
block|{
name|byte
index|[]
name|array
decl_stmt|;
name|int
name|offset
decl_stmt|;
name|int
name|len
init|=
name|row
operator|.
name|remaining
argument_list|()
decl_stmt|;
if|if
condition|(
name|row
operator|.
name|hasArray
argument_list|()
condition|)
block|{
name|array
operator|=
name|row
operator|.
name|array
argument_list|()
expr_stmt|;
name|offset
operator|=
name|row
operator|.
name|position
argument_list|()
operator|+
name|row
operator|.
name|arrayOffset
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// We copy the row array if offheap just so we can do a compare. We do this elsewhere too
comment|// in BBUtils when Cell is backed by an offheap ByteBuffer. Needs fixing so no copy. TODO.
name|array
operator|=
operator|new
name|byte
index|[
name|len
index|]
expr_stmt|;
name|offset
operator|=
literal|0
expr_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromBufferToArray
argument_list|(
name|array
argument_list|,
name|row
argument_list|,
name|row
operator|.
name|position
argument_list|()
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
comment|// Reverse result since we swap the order of the params we pass below.
return|return
operator|-
name|compareRows
argument_list|(
name|cell
argument_list|,
name|array
argument_list|,
name|offset
argument_list|,
name|len
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Comparator
name|getSimpleComparator
parameter_list|()
block|{
return|return
name|this
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Comparator
name|getSimpleComparator
parameter_list|()
block|{
return|return
operator|new
name|BBKVComparator
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|/**    * Utility method that makes a guess at comparator to use based off passed tableName.    * Use in extreme when no comparator specified.    * @return CellComparator to use going off the {@code tableName} passed.    */
specifier|public
specifier|static
name|CellComparator
name|getCellComparator
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
name|getCellComparator
argument_list|(
name|tableName
operator|.
name|toBytes
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Utility method that makes a guess at comparator to use based off passed tableName.    * Use in extreme when no comparator specified.    * @return CellComparator to use going off the {@code tableName} passed.    */
specifier|public
specifier|static
name|CellComparator
name|getCellComparator
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
block|{
comment|// FYI, TableName.toBytes does not create an array; just returns existing array pointer.
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|tableName
argument_list|,
name|TableName
operator|.
name|META_TABLE_NAME
operator|.
name|toBytes
argument_list|()
argument_list|)
condition|?
name|CellComparatorImpl
operator|.
name|META_COMPARATOR
else|:
name|CellComparatorImpl
operator|.
name|COMPARATOR
return|;
block|}
block|}
end_class

end_unit


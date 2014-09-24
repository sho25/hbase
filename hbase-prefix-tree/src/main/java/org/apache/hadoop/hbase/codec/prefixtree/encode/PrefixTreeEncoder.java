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
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|encode
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|classification
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
name|hadoop
operator|.
name|hbase
operator|.
name|Cell
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
name|CellUtil
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
name|KeyValueUtil
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
name|codec
operator|.
name|prefixtree
operator|.
name|PrefixTreeBlockMeta
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
name|codec
operator|.
name|prefixtree
operator|.
name|encode
operator|.
name|column
operator|.
name|ColumnSectionWriter
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
name|codec
operator|.
name|prefixtree
operator|.
name|encode
operator|.
name|other
operator|.
name|CellTypeEncoder
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
name|codec
operator|.
name|prefixtree
operator|.
name|encode
operator|.
name|other
operator|.
name|ColumnNodeType
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
name|codec
operator|.
name|prefixtree
operator|.
name|encode
operator|.
name|other
operator|.
name|LongEncoder
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
name|codec
operator|.
name|prefixtree
operator|.
name|encode
operator|.
name|row
operator|.
name|RowSectionWriter
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
name|codec
operator|.
name|prefixtree
operator|.
name|encode
operator|.
name|tokenize
operator|.
name|Tokenizer
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
name|io
operator|.
name|CellOutputStream
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
name|ArrayUtils
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
name|ByteRange
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
name|SimpleMutableByteRange
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
name|byterange
operator|.
name|ByteRangeSet
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
name|byterange
operator|.
name|impl
operator|.
name|ByteRangeHashSet
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
name|byterange
operator|.
name|impl
operator|.
name|ByteRangeTreeSet
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
name|vint
operator|.
name|UFIntTool
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
name|io
operator|.
name|WritableUtils
import|;
end_import

begin_comment
comment|/**  * This is the primary class for converting a CellOutputStream into an encoded byte[]. As Cells are  * added they are completely copied into the various encoding structures. This is important because  * usually the cells being fed in during compactions will be transient.<br/>  *<br/>  * Usage:<br/>  * 1) constructor<br/>  * 4) append cells in sorted order: write(Cell cell)<br/>  * 5) flush()<br/>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|PrefixTreeEncoder
implements|implements
name|CellOutputStream
block|{
comment|/**************** static ************************/
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|PrefixTreeEncoder
operator|.
name|class
argument_list|)
decl_stmt|;
comment|//future-proof where HBase supports multiple families in a data block.
specifier|public
specifier|static
specifier|final
name|boolean
name|MULITPLE_FAMILIES_POSSIBLE
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|USE_HASH_COLUMN_SORTER
init|=
literal|true
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|INITIAL_PER_CELL_ARRAY_SIZES
init|=
literal|256
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|VALUE_BUFFER_INIT_SIZE
init|=
literal|64
operator|*
literal|1024
decl_stmt|;
comment|/**************** fields *************************/
specifier|protected
name|long
name|numResets
init|=
literal|0L
decl_stmt|;
specifier|protected
name|OutputStream
name|outputStream
decl_stmt|;
comment|/*    * Cannot change during a single block's encoding. If false, then substitute incoming Cell's    * mvccVersion with zero and write out the block as usual.    */
specifier|protected
name|boolean
name|includeMvccVersion
decl_stmt|;
comment|/*    * reusable ByteRanges used for communicating with the sorters/compilers    */
specifier|protected
name|ByteRange
name|rowRange
decl_stmt|;
specifier|protected
name|ByteRange
name|familyRange
decl_stmt|;
specifier|protected
name|ByteRange
name|qualifierRange
decl_stmt|;
specifier|protected
name|ByteRange
name|tagsRange
decl_stmt|;
comment|/*    * incoming Cell fields are copied into these arrays    */
specifier|protected
name|long
index|[]
name|timestamps
decl_stmt|;
specifier|protected
name|long
index|[]
name|mvccVersions
decl_stmt|;
specifier|protected
name|byte
index|[]
name|typeBytes
decl_stmt|;
specifier|protected
name|int
index|[]
name|valueOffsets
decl_stmt|;
specifier|protected
name|int
index|[]
name|tagsOffsets
decl_stmt|;
specifier|protected
name|byte
index|[]
name|values
decl_stmt|;
specifier|protected
name|byte
index|[]
name|tags
decl_stmt|;
specifier|protected
name|PrefixTreeBlockMeta
name|blockMeta
decl_stmt|;
comment|/*    * Sub-encoders for the simple long/byte fields of a Cell.  Add to these as each cell arrives and    * compile before flushing.    */
specifier|protected
name|LongEncoder
name|timestampEncoder
decl_stmt|;
specifier|protected
name|LongEncoder
name|mvccVersionEncoder
decl_stmt|;
specifier|protected
name|CellTypeEncoder
name|cellTypeEncoder
decl_stmt|;
comment|/*    * Structures used for collecting families and qualifiers, de-duplicating them, and sorting them    * so they can be passed to the tokenizers. Unlike row keys where we can detect duplicates by    * comparing only with the previous row key, families and qualifiers can arrive in unsorted order    * in blocks spanning multiple rows. We must collect them all into a set to de-duplicate them.    */
specifier|protected
name|ByteRangeSet
name|familyDeduplicator
decl_stmt|;
specifier|protected
name|ByteRangeSet
name|qualifierDeduplicator
decl_stmt|;
specifier|protected
name|ByteRangeSet
name|tagsDeduplicator
decl_stmt|;
comment|/*    * Feed sorted byte[]s into these tokenizers which will convert the byte[]s to an in-memory    * trie structure with nodes connected by memory pointers (not serializable yet).    */
specifier|protected
name|Tokenizer
name|rowTokenizer
decl_stmt|;
specifier|protected
name|Tokenizer
name|familyTokenizer
decl_stmt|;
specifier|protected
name|Tokenizer
name|qualifierTokenizer
decl_stmt|;
specifier|protected
name|Tokenizer
name|tagsTokenizer
decl_stmt|;
comment|/*    * Writers take an in-memory trie, sort the nodes, calculate offsets and lengths, and write    * all information to an output stream of bytes that can be stored on disk.    */
specifier|protected
name|RowSectionWriter
name|rowWriter
decl_stmt|;
specifier|protected
name|ColumnSectionWriter
name|familyWriter
decl_stmt|;
specifier|protected
name|ColumnSectionWriter
name|qualifierWriter
decl_stmt|;
specifier|protected
name|ColumnSectionWriter
name|tagsWriter
decl_stmt|;
comment|/*    * Integers used for counting cells and bytes.  We keep track of the size of the Cells as if they    * were full KeyValues because some parts of HBase like to know the "unencoded size".    */
specifier|protected
name|int
name|totalCells
init|=
literal|0
decl_stmt|;
specifier|protected
name|int
name|totalUnencodedBytes
init|=
literal|0
decl_stmt|;
comment|//numBytes if the cells were KeyValues
specifier|protected
name|int
name|totalValueBytes
init|=
literal|0
decl_stmt|;
specifier|protected
name|int
name|totalTagBytes
init|=
literal|0
decl_stmt|;
specifier|protected
name|int
name|maxValueLength
init|=
literal|0
decl_stmt|;
specifier|protected
name|int
name|maxTagLength
init|=
literal|0
decl_stmt|;
specifier|protected
name|int
name|totalBytes
init|=
literal|0
decl_stmt|;
comment|//
comment|/***************** construct ***********************/
specifier|public
name|PrefixTreeEncoder
parameter_list|(
name|OutputStream
name|outputStream
parameter_list|,
name|boolean
name|includeMvccVersion
parameter_list|)
block|{
comment|// used during cell accumulation
name|this
operator|.
name|blockMeta
operator|=
operator|new
name|PrefixTreeBlockMeta
argument_list|()
expr_stmt|;
name|this
operator|.
name|rowRange
operator|=
operator|new
name|SimpleMutableByteRange
argument_list|()
expr_stmt|;
name|this
operator|.
name|familyRange
operator|=
operator|new
name|SimpleMutableByteRange
argument_list|()
expr_stmt|;
name|this
operator|.
name|qualifierRange
operator|=
operator|new
name|SimpleMutableByteRange
argument_list|()
expr_stmt|;
name|this
operator|.
name|timestamps
operator|=
operator|new
name|long
index|[
name|INITIAL_PER_CELL_ARRAY_SIZES
index|]
expr_stmt|;
name|this
operator|.
name|mvccVersions
operator|=
operator|new
name|long
index|[
name|INITIAL_PER_CELL_ARRAY_SIZES
index|]
expr_stmt|;
name|this
operator|.
name|typeBytes
operator|=
operator|new
name|byte
index|[
name|INITIAL_PER_CELL_ARRAY_SIZES
index|]
expr_stmt|;
name|this
operator|.
name|valueOffsets
operator|=
operator|new
name|int
index|[
name|INITIAL_PER_CELL_ARRAY_SIZES
index|]
expr_stmt|;
name|this
operator|.
name|values
operator|=
operator|new
name|byte
index|[
name|VALUE_BUFFER_INIT_SIZE
index|]
expr_stmt|;
comment|// used during compilation
name|this
operator|.
name|familyDeduplicator
operator|=
name|USE_HASH_COLUMN_SORTER
condition|?
operator|new
name|ByteRangeHashSet
argument_list|()
else|:
operator|new
name|ByteRangeTreeSet
argument_list|()
expr_stmt|;
name|this
operator|.
name|qualifierDeduplicator
operator|=
name|USE_HASH_COLUMN_SORTER
condition|?
operator|new
name|ByteRangeHashSet
argument_list|()
else|:
operator|new
name|ByteRangeTreeSet
argument_list|()
expr_stmt|;
name|this
operator|.
name|timestampEncoder
operator|=
operator|new
name|LongEncoder
argument_list|()
expr_stmt|;
name|this
operator|.
name|mvccVersionEncoder
operator|=
operator|new
name|LongEncoder
argument_list|()
expr_stmt|;
name|this
operator|.
name|cellTypeEncoder
operator|=
operator|new
name|CellTypeEncoder
argument_list|()
expr_stmt|;
name|this
operator|.
name|rowTokenizer
operator|=
operator|new
name|Tokenizer
argument_list|()
expr_stmt|;
name|this
operator|.
name|familyTokenizer
operator|=
operator|new
name|Tokenizer
argument_list|()
expr_stmt|;
name|this
operator|.
name|qualifierTokenizer
operator|=
operator|new
name|Tokenizer
argument_list|()
expr_stmt|;
name|this
operator|.
name|rowWriter
operator|=
operator|new
name|RowSectionWriter
argument_list|()
expr_stmt|;
name|this
operator|.
name|familyWriter
operator|=
operator|new
name|ColumnSectionWriter
argument_list|()
expr_stmt|;
name|this
operator|.
name|qualifierWriter
operator|=
operator|new
name|ColumnSectionWriter
argument_list|()
expr_stmt|;
name|initializeTagHelpers
argument_list|()
expr_stmt|;
name|reset
argument_list|(
name|outputStream
argument_list|,
name|includeMvccVersion
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|reset
parameter_list|(
name|OutputStream
name|outputStream
parameter_list|,
name|boolean
name|includeMvccVersion
parameter_list|)
block|{
operator|++
name|numResets
expr_stmt|;
name|this
operator|.
name|includeMvccVersion
operator|=
name|includeMvccVersion
expr_stmt|;
name|this
operator|.
name|outputStream
operator|=
name|outputStream
expr_stmt|;
name|valueOffsets
index|[
literal|0
index|]
operator|=
literal|0
expr_stmt|;
name|familyDeduplicator
operator|.
name|reset
argument_list|()
expr_stmt|;
name|qualifierDeduplicator
operator|.
name|reset
argument_list|()
expr_stmt|;
name|tagsDeduplicator
operator|.
name|reset
argument_list|()
expr_stmt|;
name|tagsWriter
operator|.
name|reset
argument_list|()
expr_stmt|;
name|tagsTokenizer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|rowTokenizer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|timestampEncoder
operator|.
name|reset
argument_list|()
expr_stmt|;
name|mvccVersionEncoder
operator|.
name|reset
argument_list|()
expr_stmt|;
name|cellTypeEncoder
operator|.
name|reset
argument_list|()
expr_stmt|;
name|familyTokenizer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|qualifierTokenizer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|rowWriter
operator|.
name|reset
argument_list|()
expr_stmt|;
name|familyWriter
operator|.
name|reset
argument_list|()
expr_stmt|;
name|qualifierWriter
operator|.
name|reset
argument_list|()
expr_stmt|;
name|totalCells
operator|=
literal|0
expr_stmt|;
name|totalUnencodedBytes
operator|=
literal|0
expr_stmt|;
name|totalValueBytes
operator|=
literal|0
expr_stmt|;
name|maxValueLength
operator|=
literal|0
expr_stmt|;
name|totalBytes
operator|=
literal|0
expr_stmt|;
block|}
specifier|protected
name|void
name|initializeTagHelpers
parameter_list|()
block|{
name|this
operator|.
name|tagsRange
operator|=
operator|new
name|SimpleMutableByteRange
argument_list|()
expr_stmt|;
name|this
operator|.
name|tagsDeduplicator
operator|=
name|USE_HASH_COLUMN_SORTER
condition|?
operator|new
name|ByteRangeHashSet
argument_list|()
else|:
operator|new
name|ByteRangeTreeSet
argument_list|()
expr_stmt|;
name|this
operator|.
name|tagsTokenizer
operator|=
operator|new
name|Tokenizer
argument_list|()
expr_stmt|;
name|this
operator|.
name|tagsWriter
operator|=
operator|new
name|ColumnSectionWriter
argument_list|()
expr_stmt|;
block|}
comment|/**    * Check that the arrays used to hold cell fragments are large enough for the cell that is being    * added. Since the PrefixTreeEncoder is cached between uses, these arrays may grow during the    * first few block encodings but should stabilize quickly.    */
specifier|protected
name|void
name|ensurePerCellCapacities
parameter_list|()
block|{
name|int
name|currentCapacity
init|=
name|valueOffsets
operator|.
name|length
decl_stmt|;
name|int
name|neededCapacity
init|=
name|totalCells
operator|+
literal|2
decl_stmt|;
comment|// some things write one index ahead. +2 to be safe
if|if
condition|(
name|neededCapacity
operator|<
name|currentCapacity
condition|)
block|{
return|return;
block|}
name|int
name|padding
init|=
name|neededCapacity
decl_stmt|;
comment|//this will double the array size
name|timestamps
operator|=
name|ArrayUtils
operator|.
name|growIfNecessary
argument_list|(
name|timestamps
argument_list|,
name|neededCapacity
argument_list|,
name|padding
argument_list|)
expr_stmt|;
name|mvccVersions
operator|=
name|ArrayUtils
operator|.
name|growIfNecessary
argument_list|(
name|mvccVersions
argument_list|,
name|neededCapacity
argument_list|,
name|padding
argument_list|)
expr_stmt|;
name|typeBytes
operator|=
name|ArrayUtils
operator|.
name|growIfNecessary
argument_list|(
name|typeBytes
argument_list|,
name|neededCapacity
argument_list|,
name|padding
argument_list|)
expr_stmt|;
name|valueOffsets
operator|=
name|ArrayUtils
operator|.
name|growIfNecessary
argument_list|(
name|valueOffsets
argument_list|,
name|neededCapacity
argument_list|,
name|padding
argument_list|)
expr_stmt|;
block|}
comment|/******************** CellOutputStream methods *************************/
comment|/**    * Note: Unused until support is added to the scanner/heap    *<p/>    * The following method are optimized versions of write(Cell cell). The result should be    * identical, however the implementation may be able to execute them much more efficiently because    * it does not need to compare the unchanged fields with the previous cell's.    *<p/>    * Consider the benefits during compaction when paired with a CellScanner that is also aware of    * row boundaries. The CellScanner can easily use these methods instead of blindly passing Cells    * to the write(Cell cell) method.    *<p/>    * The savings of skipping duplicate row detection are significant with long row keys. A    * DataBlockEncoder may store a row key once in combination with a count of how many cells are in    * the row. With a 100 byte row key, we can replace 100 byte comparisons with a single increment    * of the counter, and that is for every cell in the row.    */
comment|/**    * Add a Cell to the output stream but repeat the previous row.     */
comment|//@Override
specifier|public
name|void
name|writeWithRepeatRow
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|ensurePerCellCapacities
argument_list|()
expr_stmt|;
comment|//can we optimize away some of this?
comment|//save a relatively expensive row comparison, incrementing the row's counter instead
name|rowTokenizer
operator|.
name|incrementNumOccurrencesOfLatestValue
argument_list|()
expr_stmt|;
name|addFamilyPart
argument_list|(
name|cell
argument_list|)
expr_stmt|;
name|addQualifierPart
argument_list|(
name|cell
argument_list|)
expr_stmt|;
name|addAfterRowFamilyQualifier
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|ensurePerCellCapacities
argument_list|()
expr_stmt|;
name|rowTokenizer
operator|.
name|addSorted
argument_list|(
name|CellUtil
operator|.
name|fillRowRange
argument_list|(
name|cell
argument_list|,
name|rowRange
argument_list|)
argument_list|)
expr_stmt|;
name|addFamilyPart
argument_list|(
name|cell
argument_list|)
expr_stmt|;
name|addQualifierPart
argument_list|(
name|cell
argument_list|)
expr_stmt|;
name|addTagPart
argument_list|(
name|cell
argument_list|)
expr_stmt|;
name|addAfterRowFamilyQualifier
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addTagPart
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|CellUtil
operator|.
name|fillTagRange
argument_list|(
name|cell
argument_list|,
name|tagsRange
argument_list|)
expr_stmt|;
name|tagsDeduplicator
operator|.
name|add
argument_list|(
name|tagsRange
argument_list|)
expr_stmt|;
block|}
comment|/***************** internal add methods ************************/
specifier|private
name|void
name|addAfterRowFamilyQualifier
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
comment|// timestamps
name|timestamps
index|[
name|totalCells
index|]
operator|=
name|cell
operator|.
name|getTimestamp
argument_list|()
expr_stmt|;
name|timestampEncoder
operator|.
name|add
argument_list|(
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
comment|// memstore timestamps
if|if
condition|(
name|includeMvccVersion
condition|)
block|{
name|mvccVersions
index|[
name|totalCells
index|]
operator|=
name|cell
operator|.
name|getMvccVersion
argument_list|()
expr_stmt|;
name|mvccVersionEncoder
operator|.
name|add
argument_list|(
name|cell
operator|.
name|getMvccVersion
argument_list|()
argument_list|)
expr_stmt|;
name|totalUnencodedBytes
operator|+=
name|WritableUtils
operator|.
name|getVIntSize
argument_list|(
name|cell
operator|.
name|getMvccVersion
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|//must overwrite in case there was a previous version in this array slot
name|mvccVersions
index|[
name|totalCells
index|]
operator|=
literal|0L
expr_stmt|;
if|if
condition|(
name|totalCells
operator|==
literal|0
condition|)
block|{
comment|//only need to do this for the first cell added
name|mvccVersionEncoder
operator|.
name|add
argument_list|(
literal|0L
argument_list|)
expr_stmt|;
block|}
comment|//totalUncompressedBytes += 0;//mvccVersion takes zero bytes when disabled
block|}
comment|// types
name|typeBytes
index|[
name|totalCells
index|]
operator|=
name|cell
operator|.
name|getTypeByte
argument_list|()
expr_stmt|;
name|cellTypeEncoder
operator|.
name|add
argument_list|(
name|cell
operator|.
name|getTypeByte
argument_list|()
argument_list|)
expr_stmt|;
comment|// values
name|totalValueBytes
operator|+=
name|cell
operator|.
name|getValueLength
argument_list|()
expr_stmt|;
comment|// double the array each time we run out of space
name|values
operator|=
name|ArrayUtils
operator|.
name|growIfNecessary
argument_list|(
name|values
argument_list|,
name|totalValueBytes
argument_list|,
literal|2
operator|*
name|totalValueBytes
argument_list|)
expr_stmt|;
name|CellUtil
operator|.
name|copyValueTo
argument_list|(
name|cell
argument_list|,
name|values
argument_list|,
name|valueOffsets
index|[
name|totalCells
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|cell
operator|.
name|getValueLength
argument_list|()
operator|>
name|maxValueLength
condition|)
block|{
name|maxValueLength
operator|=
name|cell
operator|.
name|getValueLength
argument_list|()
expr_stmt|;
block|}
name|valueOffsets
index|[
name|totalCells
operator|+
literal|1
index|]
operator|=
name|totalValueBytes
expr_stmt|;
comment|// general
name|totalUnencodedBytes
operator|+=
name|KeyValueUtil
operator|.
name|length
argument_list|(
name|cell
argument_list|)
expr_stmt|;
operator|++
name|totalCells
expr_stmt|;
block|}
specifier|private
name|void
name|addFamilyPart
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
if|if
condition|(
name|MULITPLE_FAMILIES_POSSIBLE
operator|||
name|totalCells
operator|==
literal|0
condition|)
block|{
name|CellUtil
operator|.
name|fillFamilyRange
argument_list|(
name|cell
argument_list|,
name|familyRange
argument_list|)
expr_stmt|;
name|familyDeduplicator
operator|.
name|add
argument_list|(
name|familyRange
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|addQualifierPart
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|CellUtil
operator|.
name|fillQualifierRange
argument_list|(
name|cell
argument_list|,
name|qualifierRange
argument_list|)
expr_stmt|;
name|qualifierDeduplicator
operator|.
name|add
argument_list|(
name|qualifierRange
argument_list|)
expr_stmt|;
block|}
comment|/****************** compiling/flushing ********************/
comment|/**    * Expensive method.  The second half of the encoding work happens here.    *    * Take all the separate accumulated data structures and turn them into a single stream of bytes    * which is written to the outputStream.    */
annotation|@
name|Override
specifier|public
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
block|{
name|compile
argument_list|()
expr_stmt|;
comment|// do the actual flushing to the output stream.  Order matters.
name|blockMeta
operator|.
name|writeVariableBytesToOutputStream
argument_list|(
name|outputStream
argument_list|)
expr_stmt|;
name|rowWriter
operator|.
name|writeBytes
argument_list|(
name|outputStream
argument_list|)
expr_stmt|;
name|familyWriter
operator|.
name|writeBytes
argument_list|(
name|outputStream
argument_list|)
expr_stmt|;
name|qualifierWriter
operator|.
name|writeBytes
argument_list|(
name|outputStream
argument_list|)
expr_stmt|;
name|tagsWriter
operator|.
name|writeBytes
argument_list|(
name|outputStream
argument_list|)
expr_stmt|;
name|timestampEncoder
operator|.
name|writeBytes
argument_list|(
name|outputStream
argument_list|)
expr_stmt|;
name|mvccVersionEncoder
operator|.
name|writeBytes
argument_list|(
name|outputStream
argument_list|)
expr_stmt|;
comment|//CellType bytes are in the row nodes.  there is no additional type section
name|outputStream
operator|.
name|write
argument_list|(
name|values
argument_list|,
literal|0
argument_list|,
name|totalValueBytes
argument_list|)
expr_stmt|;
block|}
comment|/**    * Now that all the cells have been added, do the work to reduce them to a series of byte[]    * fragments that are ready to be written to the output stream.    */
specifier|protected
name|void
name|compile
parameter_list|()
block|{
name|blockMeta
operator|.
name|setNumKeyValueBytes
argument_list|(
name|totalUnencodedBytes
argument_list|)
expr_stmt|;
name|int
name|lastValueOffset
init|=
name|valueOffsets
index|[
name|totalCells
index|]
decl_stmt|;
name|blockMeta
operator|.
name|setValueOffsetWidth
argument_list|(
name|UFIntTool
operator|.
name|numBytes
argument_list|(
name|lastValueOffset
argument_list|)
argument_list|)
expr_stmt|;
name|blockMeta
operator|.
name|setValueLengthWidth
argument_list|(
name|UFIntTool
operator|.
name|numBytes
argument_list|(
name|maxValueLength
argument_list|)
argument_list|)
expr_stmt|;
name|blockMeta
operator|.
name|setNumValueBytes
argument_list|(
name|totalValueBytes
argument_list|)
expr_stmt|;
name|totalBytes
operator|+=
name|totalTagBytes
operator|+
name|totalValueBytes
expr_stmt|;
comment|//these compile methods will add to totalBytes
name|compileTypes
argument_list|()
expr_stmt|;
name|compileMvccVersions
argument_list|()
expr_stmt|;
name|compileTimestamps
argument_list|()
expr_stmt|;
name|compileTags
argument_list|()
expr_stmt|;
name|compileQualifiers
argument_list|()
expr_stmt|;
name|compileFamilies
argument_list|()
expr_stmt|;
name|compileRows
argument_list|()
expr_stmt|;
name|int
name|numMetaBytes
init|=
name|blockMeta
operator|.
name|calculateNumMetaBytes
argument_list|()
decl_stmt|;
name|blockMeta
operator|.
name|setNumMetaBytes
argument_list|(
name|numMetaBytes
argument_list|)
expr_stmt|;
name|totalBytes
operator|+=
name|numMetaBytes
expr_stmt|;
block|}
comment|/**    * The following "compile" methods do any intermediate work necessary to transform the cell    * fragments collected during the writing phase into structures that are ready to write to the    * outputStream.    *<p/>    * The family and qualifier treatment is almost identical, as is timestamp and mvccVersion.    */
specifier|protected
name|void
name|compileTypes
parameter_list|()
block|{
name|blockMeta
operator|.
name|setAllSameType
argument_list|(
name|cellTypeEncoder
operator|.
name|areAllSameType
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|cellTypeEncoder
operator|.
name|areAllSameType
argument_list|()
condition|)
block|{
name|blockMeta
operator|.
name|setAllTypes
argument_list|(
name|cellTypeEncoder
operator|.
name|getOnlyType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|compileMvccVersions
parameter_list|()
block|{
name|mvccVersionEncoder
operator|.
name|compile
argument_list|()
expr_stmt|;
name|blockMeta
operator|.
name|setMvccVersionFields
argument_list|(
name|mvccVersionEncoder
argument_list|)
expr_stmt|;
name|int
name|numMvccVersionBytes
init|=
name|mvccVersionEncoder
operator|.
name|getOutputArrayLength
argument_list|()
decl_stmt|;
name|totalBytes
operator|+=
name|numMvccVersionBytes
expr_stmt|;
block|}
specifier|protected
name|void
name|compileTimestamps
parameter_list|()
block|{
name|timestampEncoder
operator|.
name|compile
argument_list|()
expr_stmt|;
name|blockMeta
operator|.
name|setTimestampFields
argument_list|(
name|timestampEncoder
argument_list|)
expr_stmt|;
name|int
name|numTimestampBytes
init|=
name|timestampEncoder
operator|.
name|getOutputArrayLength
argument_list|()
decl_stmt|;
name|totalBytes
operator|+=
name|numTimestampBytes
expr_stmt|;
block|}
specifier|protected
name|void
name|compileQualifiers
parameter_list|()
block|{
name|blockMeta
operator|.
name|setNumUniqueQualifiers
argument_list|(
name|qualifierDeduplicator
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|qualifierDeduplicator
operator|.
name|compile
argument_list|()
expr_stmt|;
name|qualifierTokenizer
operator|.
name|addAll
argument_list|(
name|qualifierDeduplicator
operator|.
name|getSortedRanges
argument_list|()
argument_list|)
expr_stmt|;
name|qualifierWriter
operator|.
name|reconstruct
argument_list|(
name|blockMeta
argument_list|,
name|qualifierTokenizer
argument_list|,
name|ColumnNodeType
operator|.
name|QUALIFIER
argument_list|)
expr_stmt|;
name|qualifierWriter
operator|.
name|compile
argument_list|()
expr_stmt|;
name|int
name|numQualifierBytes
init|=
name|qualifierWriter
operator|.
name|getNumBytes
argument_list|()
decl_stmt|;
name|blockMeta
operator|.
name|setNumQualifierBytes
argument_list|(
name|numQualifierBytes
argument_list|)
expr_stmt|;
name|totalBytes
operator|+=
name|numQualifierBytes
expr_stmt|;
block|}
specifier|protected
name|void
name|compileFamilies
parameter_list|()
block|{
name|blockMeta
operator|.
name|setNumUniqueFamilies
argument_list|(
name|familyDeduplicator
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|familyDeduplicator
operator|.
name|compile
argument_list|()
expr_stmt|;
name|familyTokenizer
operator|.
name|addAll
argument_list|(
name|familyDeduplicator
operator|.
name|getSortedRanges
argument_list|()
argument_list|)
expr_stmt|;
name|familyWriter
operator|.
name|reconstruct
argument_list|(
name|blockMeta
argument_list|,
name|familyTokenizer
argument_list|,
name|ColumnNodeType
operator|.
name|FAMILY
argument_list|)
expr_stmt|;
name|familyWriter
operator|.
name|compile
argument_list|()
expr_stmt|;
name|int
name|numFamilyBytes
init|=
name|familyWriter
operator|.
name|getNumBytes
argument_list|()
decl_stmt|;
name|blockMeta
operator|.
name|setNumFamilyBytes
argument_list|(
name|numFamilyBytes
argument_list|)
expr_stmt|;
name|totalBytes
operator|+=
name|numFamilyBytes
expr_stmt|;
block|}
specifier|protected
name|void
name|compileTags
parameter_list|()
block|{
name|blockMeta
operator|.
name|setNumUniqueTags
argument_list|(
name|tagsDeduplicator
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|tagsDeduplicator
operator|.
name|compile
argument_list|()
expr_stmt|;
name|tagsTokenizer
operator|.
name|addAll
argument_list|(
name|tagsDeduplicator
operator|.
name|getSortedRanges
argument_list|()
argument_list|)
expr_stmt|;
name|tagsWriter
operator|.
name|reconstruct
argument_list|(
name|blockMeta
argument_list|,
name|tagsTokenizer
argument_list|,
name|ColumnNodeType
operator|.
name|TAGS
argument_list|)
expr_stmt|;
name|tagsWriter
operator|.
name|compile
argument_list|()
expr_stmt|;
name|int
name|numTagBytes
init|=
name|tagsWriter
operator|.
name|getNumBytes
argument_list|()
decl_stmt|;
name|blockMeta
operator|.
name|setNumTagsBytes
argument_list|(
name|numTagBytes
argument_list|)
expr_stmt|;
name|totalBytes
operator|+=
name|numTagBytes
expr_stmt|;
block|}
specifier|protected
name|void
name|compileRows
parameter_list|()
block|{
name|rowWriter
operator|.
name|reconstruct
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|rowWriter
operator|.
name|compile
argument_list|()
expr_stmt|;
name|int
name|numRowBytes
init|=
name|rowWriter
operator|.
name|getNumBytes
argument_list|()
decl_stmt|;
name|blockMeta
operator|.
name|setNumRowBytes
argument_list|(
name|numRowBytes
argument_list|)
expr_stmt|;
name|blockMeta
operator|.
name|setRowTreeDepth
argument_list|(
name|rowTokenizer
operator|.
name|getTreeDepth
argument_list|()
argument_list|)
expr_stmt|;
name|totalBytes
operator|+=
name|numRowBytes
expr_stmt|;
block|}
comment|/********************* convenience getters ********************************/
specifier|public
name|long
name|getValueOffset
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|valueOffsets
index|[
name|index
index|]
return|;
block|}
specifier|public
name|int
name|getValueLength
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
call|(
name|int
call|)
argument_list|(
name|valueOffsets
index|[
name|index
operator|+
literal|1
index|]
operator|-
name|valueOffsets
index|[
name|index
index|]
argument_list|)
return|;
block|}
comment|/************************* get/set *************************************/
specifier|public
name|PrefixTreeBlockMeta
name|getBlockMeta
parameter_list|()
block|{
return|return
name|blockMeta
return|;
block|}
specifier|public
name|Tokenizer
name|getRowTokenizer
parameter_list|()
block|{
return|return
name|rowTokenizer
return|;
block|}
specifier|public
name|LongEncoder
name|getTimestampEncoder
parameter_list|()
block|{
return|return
name|timestampEncoder
return|;
block|}
specifier|public
name|int
name|getTotalBytes
parameter_list|()
block|{
return|return
name|totalBytes
return|;
block|}
specifier|public
name|long
index|[]
name|getTimestamps
parameter_list|()
block|{
return|return
name|timestamps
return|;
block|}
specifier|public
name|long
index|[]
name|getMvccVersions
parameter_list|()
block|{
return|return
name|mvccVersions
return|;
block|}
specifier|public
name|byte
index|[]
name|getTypeBytes
parameter_list|()
block|{
return|return
name|typeBytes
return|;
block|}
specifier|public
name|LongEncoder
name|getMvccVersionEncoder
parameter_list|()
block|{
return|return
name|mvccVersionEncoder
return|;
block|}
specifier|public
name|ByteRangeSet
name|getFamilySorter
parameter_list|()
block|{
return|return
name|familyDeduplicator
return|;
block|}
specifier|public
name|ByteRangeSet
name|getQualifierSorter
parameter_list|()
block|{
return|return
name|qualifierDeduplicator
return|;
block|}
specifier|public
name|ByteRangeSet
name|getTagSorter
parameter_list|()
block|{
return|return
name|tagsDeduplicator
return|;
block|}
specifier|public
name|ColumnSectionWriter
name|getFamilyWriter
parameter_list|()
block|{
return|return
name|familyWriter
return|;
block|}
specifier|public
name|ColumnSectionWriter
name|getQualifierWriter
parameter_list|()
block|{
return|return
name|qualifierWriter
return|;
block|}
specifier|public
name|ColumnSectionWriter
name|getTagWriter
parameter_list|()
block|{
return|return
name|tagsWriter
return|;
block|}
specifier|public
name|RowSectionWriter
name|getRowWriter
parameter_list|()
block|{
return|return
name|rowWriter
return|;
block|}
specifier|public
name|ByteRange
name|getValueByteRange
parameter_list|()
block|{
return|return
operator|new
name|SimpleMutableByteRange
argument_list|(
name|values
argument_list|,
literal|0
argument_list|,
name|totalValueBytes
argument_list|)
return|;
block|}
block|}
end_class

end_unit


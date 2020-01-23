begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|hfile
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
import|;
end_import

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
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|fs
operator|.
name|FSDataInputStream
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
name|fs
operator|.
name|FSDataOutputStream
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|CellComparator
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
name|CellComparatorImpl
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
name|HBaseClassTestRule
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
name|HBaseTestingUtility
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|HFileProtos
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
name|testclassification
operator|.
name|IOTests
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
name|testclassification
operator|.
name|SmallTests
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
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|ExpectedException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
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

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
block|{
name|IOTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestFixedFileTrailer
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestFixedFileTrailer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestFixedFileTrailer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_COMPARATOR_NAME_LENGTH
init|=
literal|128
decl_stmt|;
comment|/**    * The number of used fields by version. Indexed by version minus two.    * Min version that we support is V2    */
specifier|private
specifier|static
specifier|final
name|int
index|[]
name|NUM_FIELDS_BY_VERSION
init|=
operator|new
name|int
index|[]
block|{
literal|14
block|,
literal|15
block|}
decl_stmt|;
specifier|private
name|HBaseTestingUtility
name|util
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
specifier|private
name|int
name|version
decl_stmt|;
static|static
block|{
assert|assert
name|NUM_FIELDS_BY_VERSION
operator|.
name|length
operator|==
name|HFile
operator|.
name|MAX_FORMAT_VERSION
operator|-
name|HFile
operator|.
name|MIN_FORMAT_VERSION
operator|+
literal|1
assert|;
block|}
specifier|public
name|TestFixedFileTrailer
parameter_list|(
name|int
name|version
parameter_list|)
block|{
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
block|}
annotation|@
name|Rule
specifier|public
name|ExpectedException
name|expectedEx
init|=
name|ExpectedException
operator|.
name|none
argument_list|()
decl_stmt|;
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|getParameters
parameter_list|()
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|versionsToTest
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|v
init|=
name|HFile
operator|.
name|MIN_FORMAT_VERSION
init|;
name|v
operator|<=
name|HFile
operator|.
name|MAX_FORMAT_VERSION
condition|;
operator|++
name|v
control|)
name|versionsToTest
operator|.
name|add
argument_list|(
operator|new
name|Integer
index|[]
block|{
name|v
block|}
argument_list|)
expr_stmt|;
return|return
name|versionsToTest
return|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testComparatorIsHBase1Compatible
parameter_list|()
block|{
name|FixedFileTrailer
name|t
init|=
operator|new
name|FixedFileTrailer
argument_list|(
name|version
argument_list|,
name|HFileReaderImpl
operator|.
name|PBUF_TRAILER_MINOR_VERSION
argument_list|)
decl_stmt|;
name|t
operator|.
name|setComparatorClass
argument_list|(
name|CellComparatorImpl
operator|.
name|COMPARATOR
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|CellComparatorImpl
operator|.
name|COMPARATOR
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|t
operator|.
name|getComparatorClassName
argument_list|()
argument_list|)
expr_stmt|;
name|HFileProtos
operator|.
name|FileTrailerProto
name|pb
init|=
name|t
operator|.
name|toProtobuf
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|pb
operator|.
name|getComparatorClassName
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|setComparatorClass
argument_list|(
name|CellComparatorImpl
operator|.
name|MetaCellComparator
operator|.
name|META_COMPARATOR
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|pb
operator|=
name|t
operator|.
name|toProtobuf
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|KeyValue
operator|.
name|META_COMPARATOR
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|pb
operator|.
name|getComparatorClassName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateComparator
parameter_list|()
throws|throws
name|IOException
block|{
name|FixedFileTrailer
name|t
init|=
operator|new
name|FixedFileTrailer
argument_list|(
name|version
argument_list|,
name|HFileReaderImpl
operator|.
name|PBUF_TRAILER_MINOR_VERSION
argument_list|)
decl_stmt|;
try|try
block|{
name|assertEquals
argument_list|(
name|CellComparatorImpl
operator|.
name|class
argument_list|,
name|t
operator|.
name|createComparator
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
operator|.
name|getLegacyKeyComparatorName
argument_list|()
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|CellComparatorImpl
operator|.
name|class
argument_list|,
name|t
operator|.
name|createComparator
argument_list|(
name|KeyValue
operator|.
name|COMPARATOR
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|CellComparatorImpl
operator|.
name|class
argument_list|,
name|t
operator|.
name|createComparator
argument_list|(
name|CellComparator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|CellComparatorImpl
operator|.
name|MetaCellComparator
operator|.
name|class
argument_list|,
name|t
operator|.
name|createComparator
argument_list|(
name|KeyValue
operator|.
name|META_COMPARATOR
operator|.
name|getLegacyKeyComparatorName
argument_list|()
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|CellComparatorImpl
operator|.
name|MetaCellComparator
operator|.
name|class
argument_list|,
name|t
operator|.
name|createComparator
argument_list|(
name|KeyValue
operator|.
name|META_COMPARATOR
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|CellComparatorImpl
operator|.
name|MetaCellComparator
operator|.
name|class
argument_list|,
name|t
operator|.
name|createComparator
argument_list|(
name|CellComparatorImpl
operator|.
name|MetaCellComparator
operator|.
name|META_COMPARATOR
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|CellComparatorImpl
operator|.
name|META_COMPARATOR
operator|.
name|getClass
argument_list|()
argument_list|,
name|t
operator|.
name|createComparator
argument_list|(
name|CellComparatorImpl
operator|.
name|MetaCellComparator
operator|.
name|META_COMPARATOR
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|CellComparatorImpl
operator|.
name|COMPARATOR
operator|.
name|getClass
argument_list|()
argument_list|,
name|t
operator|.
name|createComparator
argument_list|(
name|CellComparatorImpl
operator|.
name|MetaCellComparator
operator|.
name|COMPARATOR
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|t
operator|.
name|createComparator
argument_list|(
name|Bytes
operator|.
name|BYTES_RAWCOMPARATOR
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|t
operator|.
name|createComparator
argument_list|(
literal|"org.apache.hadoop.hbase.KeyValue$RawBytesComparator"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Unexpected exception while testing FixedFileTrailer#createComparator()"
argument_list|)
expr_stmt|;
block|}
comment|// Test an invalid comparatorClassName
name|expectedEx
operator|.
name|expect
argument_list|(
name|IOException
operator|.
name|class
argument_list|)
expr_stmt|;
name|t
operator|.
name|createComparator
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTrailer
parameter_list|()
throws|throws
name|IOException
block|{
name|FixedFileTrailer
name|t
init|=
operator|new
name|FixedFileTrailer
argument_list|(
name|version
argument_list|,
name|HFileReaderImpl
operator|.
name|PBUF_TRAILER_MINOR_VERSION
argument_list|)
decl_stmt|;
name|t
operator|.
name|setDataIndexCount
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|t
operator|.
name|setEntryCount
argument_list|(
operator|(
operator|(
name|long
operator|)
name|Integer
operator|.
name|MAX_VALUE
operator|)
operator|+
literal|1
argument_list|)
expr_stmt|;
name|t
operator|.
name|setLastDataBlockOffset
argument_list|(
literal|291
argument_list|)
expr_stmt|;
name|t
operator|.
name|setNumDataIndexLevels
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|t
operator|.
name|setComparatorClass
argument_list|(
name|CellComparatorImpl
operator|.
name|COMPARATOR
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|setFirstDataBlockOffset
argument_list|(
literal|9081723123L
argument_list|)
expr_stmt|;
comment|// Completely unrealistic.
name|t
operator|.
name|setUncompressedDataIndexSize
argument_list|(
literal|827398717L
argument_list|)
expr_stmt|;
comment|// Something random.
name|t
operator|.
name|setLoadOnOpenOffset
argument_list|(
literal|128
argument_list|)
expr_stmt|;
name|t
operator|.
name|setMetaIndexCount
argument_list|(
literal|7
argument_list|)
expr_stmt|;
name|t
operator|.
name|setTotalUncompressedBytes
argument_list|(
literal|129731987
argument_list|)
expr_stmt|;
block|{
name|DataOutputStream
name|dos
init|=
operator|new
name|DataOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
comment|// Limited scope.
name|t
operator|.
name|serialize
argument_list|(
name|dos
argument_list|)
expr_stmt|;
name|dos
operator|.
name|flush
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|dos
operator|.
name|size
argument_list|()
argument_list|,
name|FixedFileTrailer
operator|.
name|getTrailerSize
argument_list|(
name|version
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|bytes
init|=
name|baos
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|baos
operator|.
name|reset
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|bytes
operator|.
name|length
argument_list|,
name|FixedFileTrailer
operator|.
name|getTrailerSize
argument_list|(
name|version
argument_list|)
argument_list|)
expr_stmt|;
name|ByteArrayInputStream
name|bais
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
comment|// Finished writing, trying to read.
block|{
name|DataInputStream
name|dis
init|=
operator|new
name|DataInputStream
argument_list|(
name|bais
argument_list|)
decl_stmt|;
name|FixedFileTrailer
name|t2
init|=
operator|new
name|FixedFileTrailer
argument_list|(
name|version
argument_list|,
name|HFileReaderImpl
operator|.
name|PBUF_TRAILER_MINOR_VERSION
argument_list|)
decl_stmt|;
name|t2
operator|.
name|deserialize
argument_list|(
name|dis
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|bais
operator|.
name|read
argument_list|()
argument_list|)
expr_stmt|;
comment|// Ensure we have read everything.
name|checkLoadedTrailer
argument_list|(
name|version
argument_list|,
name|t
argument_list|,
name|t2
argument_list|)
expr_stmt|;
block|}
comment|// Now check what happens if the trailer is corrupted.
name|Path
name|trailerPath
init|=
operator|new
name|Path
argument_list|(
name|util
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
literal|"trailer_"
operator|+
name|version
argument_list|)
decl_stmt|;
block|{
for|for
control|(
name|byte
name|invalidVersion
range|:
operator|new
name|byte
index|[]
block|{
name|HFile
operator|.
name|MIN_FORMAT_VERSION
operator|-
literal|1
block|,
name|HFile
operator|.
name|MAX_FORMAT_VERSION
operator|+
literal|1
block|}
control|)
block|{
name|bytes
index|[
name|bytes
operator|.
name|length
operator|-
literal|1
index|]
operator|=
name|invalidVersion
expr_stmt|;
name|writeTrailer
argument_list|(
name|trailerPath
argument_list|,
literal|null
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
try|try
block|{
name|readTrailer
argument_list|(
name|trailerPath
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Exception expected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// Make it easy to debug this.
name|String
name|msg
init|=
name|ex
operator|.
name|getMessage
argument_list|()
decl_stmt|;
name|String
name|cleanMsg
init|=
name|msg
operator|.
name|replaceAll
argument_list|(
literal|"^(java(\\.[a-zA-Z]+)+:\\s+)?|\\s+\\(.*\\)\\s*$"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Actual exception message is \""
operator|+
name|msg
operator|+
literal|"\".\n"
operator|+
literal|"Cleaned-up message"
argument_list|,
comment|// will be followed by " expected: ..."
literal|"Invalid HFile version: "
operator|+
name|invalidVersion
argument_list|,
name|cleanMsg
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Got an expected exception: "
operator|+
name|msg
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Now write the trailer into a file and auto-detect the version.
name|writeTrailer
argument_list|(
name|trailerPath
argument_list|,
name|t
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|FixedFileTrailer
name|t4
init|=
name|readTrailer
argument_list|(
name|trailerPath
argument_list|)
decl_stmt|;
name|checkLoadedTrailer
argument_list|(
name|version
argument_list|,
name|t
argument_list|,
name|t4
argument_list|)
expr_stmt|;
name|String
name|trailerStr
init|=
name|t
operator|.
name|toString
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Invalid number of fields in the string representation "
operator|+
literal|"of the trailer: "
operator|+
name|trailerStr
argument_list|,
name|NUM_FIELDS_BY_VERSION
index|[
name|version
operator|-
literal|2
index|]
argument_list|,
name|trailerStr
operator|.
name|split
argument_list|(
literal|", "
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|trailerStr
argument_list|,
name|t4
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTrailerForV2NonPBCompatibility
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|version
operator|==
literal|2
condition|)
block|{
name|FixedFileTrailer
name|t
init|=
operator|new
name|FixedFileTrailer
argument_list|(
name|version
argument_list|,
name|HFileReaderImpl
operator|.
name|MINOR_VERSION_NO_CHECKSUM
argument_list|)
decl_stmt|;
name|t
operator|.
name|setDataIndexCount
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|t
operator|.
name|setEntryCount
argument_list|(
operator|(
operator|(
name|long
operator|)
name|Integer
operator|.
name|MAX_VALUE
operator|)
operator|+
literal|1
argument_list|)
expr_stmt|;
name|t
operator|.
name|setLastDataBlockOffset
argument_list|(
literal|291
argument_list|)
expr_stmt|;
name|t
operator|.
name|setNumDataIndexLevels
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|t
operator|.
name|setComparatorClass
argument_list|(
name|CellComparatorImpl
operator|.
name|COMPARATOR
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|setFirstDataBlockOffset
argument_list|(
literal|9081723123L
argument_list|)
expr_stmt|;
comment|// Completely unrealistic.
name|t
operator|.
name|setUncompressedDataIndexSize
argument_list|(
literal|827398717L
argument_list|)
expr_stmt|;
comment|// Something random.
name|t
operator|.
name|setLoadOnOpenOffset
argument_list|(
literal|128
argument_list|)
expr_stmt|;
name|t
operator|.
name|setMetaIndexCount
argument_list|(
literal|7
argument_list|)
expr_stmt|;
name|t
operator|.
name|setTotalUncompressedBytes
argument_list|(
literal|129731987
argument_list|)
expr_stmt|;
block|{
name|DataOutputStream
name|dos
init|=
operator|new
name|DataOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
comment|// Limited scope.
name|serializeAsWritable
argument_list|(
name|dos
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|dos
operator|.
name|flush
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|FixedFileTrailer
operator|.
name|getTrailerSize
argument_list|(
name|version
argument_list|)
argument_list|,
name|dos
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|bytes
init|=
name|baos
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|baos
operator|.
name|reset
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|bytes
operator|.
name|length
argument_list|,
name|FixedFileTrailer
operator|.
name|getTrailerSize
argument_list|(
name|version
argument_list|)
argument_list|)
expr_stmt|;
name|ByteArrayInputStream
name|bais
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
block|{
name|DataInputStream
name|dis
init|=
operator|new
name|DataInputStream
argument_list|(
name|bais
argument_list|)
decl_stmt|;
name|FixedFileTrailer
name|t2
init|=
operator|new
name|FixedFileTrailer
argument_list|(
name|version
argument_list|,
name|HFileReaderImpl
operator|.
name|MINOR_VERSION_NO_CHECKSUM
argument_list|)
decl_stmt|;
name|t2
operator|.
name|deserialize
argument_list|(
name|dis
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|bais
operator|.
name|read
argument_list|()
argument_list|)
expr_stmt|;
comment|// Ensure we have read everything.
name|checkLoadedTrailer
argument_list|(
name|version
argument_list|,
name|t
argument_list|,
name|t2
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Copied from FixedFileTrailer for testing the reading part of
comment|// FixedFileTrailer of non PB
comment|// serialized FFTs.
specifier|private
name|void
name|serializeAsWritable
parameter_list|(
name|DataOutputStream
name|output
parameter_list|,
name|FixedFileTrailer
name|fft
parameter_list|)
throws|throws
name|IOException
block|{
name|BlockType
operator|.
name|TRAILER
operator|.
name|write
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeLong
argument_list|(
name|fft
operator|.
name|getFileInfoOffset
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeLong
argument_list|(
name|fft
operator|.
name|getLoadOnOpenDataOffset
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeInt
argument_list|(
name|fft
operator|.
name|getDataIndexCount
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeLong
argument_list|(
name|fft
operator|.
name|getUncompressedDataIndexSize
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeInt
argument_list|(
name|fft
operator|.
name|getMetaIndexCount
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeLong
argument_list|(
name|fft
operator|.
name|getTotalUncompressedBytes
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeLong
argument_list|(
name|fft
operator|.
name|getEntryCount
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeInt
argument_list|(
name|fft
operator|.
name|getCompressionCodec
argument_list|()
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeInt
argument_list|(
name|fft
operator|.
name|getNumDataIndexLevels
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeLong
argument_list|(
name|fft
operator|.
name|getFirstDataBlockOffset
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeLong
argument_list|(
name|fft
operator|.
name|getLastDataBlockOffset
argument_list|()
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeStringFixedSize
argument_list|(
name|output
argument_list|,
name|fft
operator|.
name|getComparatorClassName
argument_list|()
argument_list|,
name|MAX_COMPARATOR_NAME_LENGTH
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeInt
argument_list|(
name|FixedFileTrailer
operator|.
name|materializeVersion
argument_list|(
name|fft
operator|.
name|getMajorVersion
argument_list|()
argument_list|,
name|fft
operator|.
name|getMinorVersion
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|FixedFileTrailer
name|readTrailer
parameter_list|(
name|Path
name|trailerPath
parameter_list|)
throws|throws
name|IOException
block|{
name|FSDataInputStream
name|fsdis
init|=
name|fs
operator|.
name|open
argument_list|(
name|trailerPath
argument_list|)
decl_stmt|;
name|FixedFileTrailer
name|trailerRead
init|=
name|FixedFileTrailer
operator|.
name|readFromStream
argument_list|(
name|fsdis
argument_list|,
name|fs
operator|.
name|getFileStatus
argument_list|(
name|trailerPath
argument_list|)
operator|.
name|getLen
argument_list|()
argument_list|)
decl_stmt|;
name|fsdis
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|trailerRead
return|;
block|}
specifier|private
name|void
name|writeTrailer
parameter_list|(
name|Path
name|trailerPath
parameter_list|,
name|FixedFileTrailer
name|t
parameter_list|,
name|byte
index|[]
name|useBytesInstead
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
operator|(
name|t
operator|==
literal|null
operator|)
operator|!=
operator|(
name|useBytesInstead
operator|==
literal|null
operator|)
assert|;
comment|// Expect one non-null.
name|FSDataOutputStream
name|fsdos
init|=
name|fs
operator|.
name|create
argument_list|(
name|trailerPath
argument_list|)
decl_stmt|;
name|fsdos
operator|.
name|write
argument_list|(
literal|135
argument_list|)
expr_stmt|;
comment|// to make deserializer's job less trivial
if|if
condition|(
name|useBytesInstead
operator|!=
literal|null
condition|)
block|{
name|fsdos
operator|.
name|write
argument_list|(
name|useBytesInstead
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|t
operator|.
name|serialize
argument_list|(
name|fsdos
argument_list|)
expr_stmt|;
block|}
name|fsdos
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|checkLoadedTrailer
parameter_list|(
name|int
name|version
parameter_list|,
name|FixedFileTrailer
name|expected
parameter_list|,
name|FixedFileTrailer
name|loaded
parameter_list|)
throws|throws
name|IOException
block|{
name|assertEquals
argument_list|(
name|version
argument_list|,
name|loaded
operator|.
name|getMajorVersion
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getDataIndexCount
argument_list|()
argument_list|,
name|loaded
operator|.
name|getDataIndexCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Math
operator|.
name|min
argument_list|(
name|expected
operator|.
name|getEntryCount
argument_list|()
argument_list|,
name|version
operator|==
literal|1
condition|?
name|Integer
operator|.
name|MAX_VALUE
else|:
name|Long
operator|.
name|MAX_VALUE
argument_list|)
argument_list|,
name|loaded
operator|.
name|getEntryCount
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|version
operator|==
literal|1
condition|)
block|{
name|assertEquals
argument_list|(
name|expected
operator|.
name|getFileInfoOffset
argument_list|()
argument_list|,
name|loaded
operator|.
name|getFileInfoOffset
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|version
operator|==
literal|2
condition|)
block|{
name|assertEquals
argument_list|(
name|expected
operator|.
name|getLastDataBlockOffset
argument_list|()
argument_list|,
name|loaded
operator|.
name|getLastDataBlockOffset
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getNumDataIndexLevels
argument_list|()
argument_list|,
name|loaded
operator|.
name|getNumDataIndexLevels
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|createComparator
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|loaded
operator|.
name|createComparator
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getFirstDataBlockOffset
argument_list|()
argument_list|,
name|loaded
operator|.
name|getFirstDataBlockOffset
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|expected
operator|.
name|createComparator
argument_list|()
operator|instanceof
name|CellComparatorImpl
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getUncompressedDataIndexSize
argument_list|()
argument_list|,
name|loaded
operator|.
name|getUncompressedDataIndexSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expected
operator|.
name|getLoadOnOpenDataOffset
argument_list|()
argument_list|,
name|loaded
operator|.
name|getLoadOnOpenDataOffset
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getMetaIndexCount
argument_list|()
argument_list|,
name|loaded
operator|.
name|getMetaIndexCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getTotalUncompressedBytes
argument_list|()
argument_list|,
name|loaded
operator|.
name|getTotalUncompressedBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


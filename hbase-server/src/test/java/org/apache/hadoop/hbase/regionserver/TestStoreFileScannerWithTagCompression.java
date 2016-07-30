begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
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
name|assertTrue
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
name|conf
operator|.
name|Configuration
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
name|Tag
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
name|TagUtil
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
name|RegionServerTests
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
name|ArrayBackedTag
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
name|encoding
operator|.
name|DataBlockEncoding
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
name|hfile
operator|.
name|CacheConfig
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
name|hfile
operator|.
name|HFileContext
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
name|hfile
operator|.
name|HFileContextBuilder
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
name|BeforeClass
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
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
name|TestStoreFileScannerWithTagCompression
block|{
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|CacheConfig
name|cacheConf
init|=
operator|new
name|CacheConfig
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|String
name|ROOT_DIR
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"TestStoreFileScannerWithTagCompression"
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|conf
operator|.
name|setInt
argument_list|(
literal|"hfile.format.version"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReseek
parameter_list|()
throws|throws
name|Exception
block|{
comment|// write the file
name|Path
name|f
init|=
operator|new
name|Path
argument_list|(
name|ROOT_DIR
argument_list|,
literal|"testReseek"
argument_list|)
decl_stmt|;
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withBlockSize
argument_list|(
literal|8
operator|*
literal|1024
argument_list|)
operator|.
name|withIncludesTags
argument_list|(
literal|true
argument_list|)
operator|.
name|withCompressTags
argument_list|(
literal|true
argument_list|)
operator|.
name|withDataBlockEncoding
argument_list|(
name|DataBlockEncoding
operator|.
name|PREFIX
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// Make a store file and write data to it.
name|StoreFileWriter
name|writer
init|=
operator|new
name|StoreFileWriter
operator|.
name|Builder
argument_list|(
name|conf
argument_list|,
name|cacheConf
argument_list|,
name|fs
argument_list|)
operator|.
name|withFilePath
argument_list|(
name|f
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|meta
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|writeStoreFile
argument_list|(
name|writer
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|StoreFileReader
name|reader
init|=
operator|new
name|StoreFileReader
argument_list|(
name|fs
argument_list|,
name|f
argument_list|,
name|cacheConf
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|StoreFileScanner
name|s
init|=
name|reader
operator|.
name|getStoreFileScanner
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Now do reseek with empty KV to position to the beginning of the file
name|KeyValue
name|k
init|=
name|KeyValueUtil
operator|.
name|createFirstOnRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"k2"
argument_list|)
argument_list|)
decl_stmt|;
name|s
operator|.
name|reseek
argument_list|(
name|k
argument_list|)
expr_stmt|;
name|Cell
name|kv
init|=
name|s
operator|.
name|next
argument_list|()
decl_stmt|;
name|kv
operator|=
name|s
operator|.
name|next
argument_list|()
expr_stmt|;
name|kv
operator|=
name|s
operator|.
name|next
argument_list|()
expr_stmt|;
name|byte
index|[]
name|key5
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"k5"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|key5
argument_list|,
literal|0
argument_list|,
name|key5
operator|.
name|length
argument_list|,
name|kv
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
init|=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|kv
argument_list|)
operator|.
name|getTags
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tags
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"tag3"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|TagUtil
operator|.
name|cloneValue
argument_list|(
name|tags
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|writeStoreFile
parameter_list|(
specifier|final
name|StoreFileWriter
name|writer
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|fam
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
decl_stmt|;
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|byte
index|[]
name|b
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"k1"
argument_list|)
decl_stmt|;
name|Tag
name|t1
init|=
operator|new
name|ArrayBackedTag
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
literal|"tag1"
argument_list|)
decl_stmt|;
name|Tag
name|t2
init|=
operator|new
name|ArrayBackedTag
argument_list|(
operator|(
name|byte
operator|)
literal|2
argument_list|,
literal|"tag2"
argument_list|)
decl_stmt|;
name|Tag
name|t3
init|=
operator|new
name|ArrayBackedTag
argument_list|(
operator|(
name|byte
operator|)
literal|3
argument_list|,
literal|"tag3"
argument_list|)
decl_stmt|;
try|try
block|{
name|writer
operator|.
name|append
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|b
argument_list|,
name|fam
argument_list|,
name|qualifier
argument_list|,
name|now
argument_list|,
name|b
argument_list|,
operator|new
name|Tag
index|[]
block|{
name|t1
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|b
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"k3"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|b
argument_list|,
name|fam
argument_list|,
name|qualifier
argument_list|,
name|now
argument_list|,
name|b
argument_list|,
operator|new
name|Tag
index|[]
block|{
name|t2
block|,
name|t1
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|b
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"k4"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|b
argument_list|,
name|fam
argument_list|,
name|qualifier
argument_list|,
name|now
argument_list|,
name|b
argument_list|,
operator|new
name|Tag
index|[]
block|{
name|t3
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|b
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"k5"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|b
argument_list|,
name|fam
argument_list|,
name|qualifier
argument_list|,
name|now
argument_list|,
name|b
argument_list|,
operator|new
name|Tag
index|[]
block|{
name|t3
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


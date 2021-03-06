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
name|util
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|HStoreFile
operator|.
name|BULKLOAD_TIME_KEY
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
name|assertArrayEquals
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
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
name|HColumnDescriptor
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
name|PrivateCellUtil
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
name|TagType
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
name|client
operator|.
name|Result
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
name|client
operator|.
name|ResultScanner
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
name|client
operator|.
name|Scan
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
name|client
operator|.
name|Table
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
name|HFile
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
name|mob
operator|.
name|MobUtils
import|;
end_import

begin_comment
comment|/**  * Utility class for HFile-related testing.  */
end_comment

begin_class
specifier|public
class|class
name|HFileTestUtil
block|{
specifier|public
specifier|static
specifier|final
name|String
name|OPT_DATA_BLOCK_ENCODING_USAGE
init|=
literal|"Encoding algorithm (e.g. prefix "
operator|+
literal|"compression) to use for data blocks in the test column family, "
operator|+
literal|"one of "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|DataBlockEncoding
operator|.
name|values
argument_list|()
argument_list|)
operator|+
literal|"."
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OPT_DATA_BLOCK_ENCODING
init|=
name|HColumnDescriptor
operator|.
name|DATA_BLOCK_ENCODING
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
comment|/** Column family used by the test */
specifier|public
specifier|static
name|byte
index|[]
name|DEFAULT_COLUMN_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test_cf"
argument_list|)
decl_stmt|;
comment|/** Column families used by the test */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|DEFAULT_COLUMN_FAMILIES
init|=
block|{
name|DEFAULT_COLUMN_FAMILY
block|}
decl_stmt|;
comment|/**    * Create an HFile with the given number of rows between a given    * start key and end key @ family:qualifier.  The value will be the key value.    * This file will not have tags.    */
specifier|public
specifier|static
name|void
name|createHFile
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|int
name|numRows
parameter_list|)
throws|throws
name|IOException
block|{
name|createHFile
argument_list|(
name|configuration
argument_list|,
name|fs
argument_list|,
name|path
argument_list|,
name|DataBlockEncoding
operator|.
name|NONE
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
name|numRows
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an HFile with the given number of rows between a given    * start key and end key @ family:qualifier.  The value will be the key value.    * This file will use certain data block encoding algorithm.    */
specifier|public
specifier|static
name|void
name|createHFileWithDataBlockEncoding
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|DataBlockEncoding
name|encoding
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|int
name|numRows
parameter_list|)
throws|throws
name|IOException
block|{
name|createHFile
argument_list|(
name|configuration
argument_list|,
name|fs
argument_list|,
name|path
argument_list|,
name|encoding
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
name|numRows
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an HFile with the given number of rows between a given    * start key and end key @ family:qualifier.  The value will be the key value.    * This cells will also have a tag whose value is the key.    */
specifier|public
specifier|static
name|void
name|createHFileWithTags
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|int
name|numRows
parameter_list|)
throws|throws
name|IOException
block|{
name|createHFile
argument_list|(
name|configuration
argument_list|,
name|fs
argument_list|,
name|path
argument_list|,
name|DataBlockEncoding
operator|.
name|NONE
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|,
name|numRows
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an HFile with the given number of rows between a given    * start key and end key @ family:qualifier.    * If withTag is true, we add the rowKey as the tag value for    * tagtype MOB_TABLE_NAME_TAG_TYPE    */
specifier|public
specifier|static
name|void
name|createHFile
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|DataBlockEncoding
name|encoding
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|int
name|numRows
parameter_list|,
name|boolean
name|withTag
parameter_list|)
throws|throws
name|IOException
block|{
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withIncludesTags
argument_list|(
name|withTag
argument_list|)
operator|.
name|withDataBlockEncoding
argument_list|(
name|encoding
argument_list|)
operator|.
name|withColumnFamily
argument_list|(
name|family
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HFile
operator|.
name|Writer
name|writer
init|=
name|HFile
operator|.
name|getWriterFactory
argument_list|(
name|configuration
argument_list|,
operator|new
name|CacheConfig
argument_list|(
name|configuration
argument_list|)
argument_list|)
operator|.
name|withPath
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|meta
argument_list|)
operator|.
name|create
argument_list|()
decl_stmt|;
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
comment|// subtract 2 since iterateOnSplits doesn't include boundary keys
for|for
control|(
name|byte
index|[]
name|key
range|:
name|Bytes
operator|.
name|iterateOnSplits
argument_list|(
name|startKey
argument_list|,
name|endKey
argument_list|,
name|numRows
operator|-
literal|2
argument_list|)
control|)
block|{
name|Cell
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|key
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|now
argument_list|,
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|withTag
condition|)
block|{
comment|// add a tag.  Arbitrarily chose mob tag since we have a helper already.
name|Tag
name|tableNameTag
init|=
operator|new
name|ArrayBackedTag
argument_list|(
name|TagType
operator|.
name|MOB_TABLE_NAME_TAG_TYPE
argument_list|,
name|key
argument_list|)
decl_stmt|;
name|kv
operator|=
name|MobUtils
operator|.
name|createMobRefCell
argument_list|(
name|kv
argument_list|,
name|key
argument_list|,
name|tableNameTag
argument_list|)
expr_stmt|;
comment|// verify that the kv has the tag.
name|Optional
argument_list|<
name|Tag
argument_list|>
name|tag
init|=
name|PrivateCellUtil
operator|.
name|getTag
argument_list|(
name|kv
argument_list|,
name|TagType
operator|.
name|MOB_TABLE_NAME_TAG_TYPE
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|tag
operator|.
name|isPresent
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Tag didn't stick to KV "
operator|+
name|kv
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|writer
operator|.
name|append
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|writer
operator|.
name|appendFileInfo
argument_list|(
name|BULKLOAD_TIME_KEY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * This verifies that each cell has a tag that is equal to its rowkey name.  For this to work    * the hbase instance must have HConstants.RPC_CODEC_CONF_KEY set to    * KeyValueCodecWithTags.class.getCanonicalName());    * @param table table containing tagged cells    * @throws IOException if problems reading table    */
specifier|public
specifier|static
name|void
name|verifyTags
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|ResultScanner
name|s
init|=
name|table
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Result
name|r
range|:
name|s
control|)
block|{
for|for
control|(
name|Cell
name|c
range|:
name|r
operator|.
name|listCells
argument_list|()
control|)
block|{
name|Optional
argument_list|<
name|Tag
argument_list|>
name|tag
init|=
name|PrivateCellUtil
operator|.
name|getTag
argument_list|(
name|c
argument_list|,
name|TagType
operator|.
name|MOB_TABLE_NAME_TAG_TYPE
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|tag
operator|.
name|isPresent
argument_list|()
condition|)
block|{
name|fail
argument_list|(
name|c
operator|.
name|toString
argument_list|()
operator|+
literal|" has null tag"
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|Tag
name|t
init|=
name|tag
operator|.
name|get
argument_list|()
decl_stmt|;
name|byte
index|[]
name|tval
init|=
name|Tag
operator|.
name|cloneValue
argument_list|(
name|t
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|c
operator|.
name|toString
argument_list|()
operator|+
literal|" has tag"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tval
argument_list|)
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|,
name|tval
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit


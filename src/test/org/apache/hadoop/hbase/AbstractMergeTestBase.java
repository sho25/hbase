begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|UnsupportedEncodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|BatchUpdate
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
name|ImmutableBytesWritable
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
name|regionserver
operator|.
name|HRegion
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

begin_comment
comment|/** Abstract base class for merge tests */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractMergeTestBase
extends|extends
name|HBaseClusterTestCase
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|AbstractMergeTestBase
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"contents:"
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|protected
name|HTableDescriptor
name|desc
decl_stmt|;
specifier|protected
name|ImmutableBytesWritable
name|value
decl_stmt|;
specifier|protected
name|boolean
name|startMiniHBase
decl_stmt|;
specifier|public
name|AbstractMergeTestBase
parameter_list|()
block|{
name|this
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/** constructor     * @param startMiniHBase    */
specifier|public
name|AbstractMergeTestBase
parameter_list|(
name|boolean
name|startMiniHBase
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|startMiniHBase
operator|=
name|startMiniHBase
expr_stmt|;
comment|// We will use the same value for the rows as that is not really important here
name|String
name|partialValue
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|StringBuilder
name|val
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
while|while
condition|(
name|val
operator|.
name|length
argument_list|()
operator|<
literal|1024
condition|)
block|{
name|val
operator|.
name|append
argument_list|(
name|partialValue
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|value
operator|=
operator|new
name|ImmutableBytesWritable
argument_list|(
name|val
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
name|fail
argument_list|()
expr_stmt|;
block|}
name|desc
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|COLUMN_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|hBaseClusterSetup
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|startMiniHBase
condition|)
block|{
name|super
operator|.
name|hBaseClusterSetup
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|preHBaseClusterSetup
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|.
name|setLong
argument_list|(
literal|"hbase.hregion.max.filesize"
argument_list|,
literal|64L
operator|*
literal|1024L
operator|*
literal|1024L
argument_list|)
expr_stmt|;
comment|// We create three data regions: The first is too large to merge since it
comment|// will be> 64 MB in size. The second two will be smaller and will be
comment|// selected for merging.
comment|// To ensure that the first region is larger than 64MB we need to write at
comment|// least 65536 rows. We will make certain by writing 70000
name|byte
index|[]
name|row_70001
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_70001"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row_80001
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_80001"
argument_list|)
decl_stmt|;
comment|// XXX: Note that the number of rows we put in is different for each region
comment|// because currently we don't have a good mechanism to handle merging two
comment|// store files with the same sequence id. We can't just dumbly stick them
comment|// in because it will screw up the order when the store files are loaded up.
comment|// The sequence ids are used for arranging the store files, so if two files
comment|// have the same id, one will overwrite the other one in our listing, which
comment|// is very bad. See HBASE-1212 and HBASE-1274.
name|HRegion
index|[]
name|regions
init|=
block|{
name|createAregion
argument_list|(
literal|null
argument_list|,
name|row_70001
argument_list|,
literal|1
argument_list|,
literal|70000
argument_list|)
block|,
name|createAregion
argument_list|(
name|row_70001
argument_list|,
name|row_80001
argument_list|,
literal|70001
argument_list|,
literal|10000
argument_list|)
block|,
name|createAregion
argument_list|(
name|row_80001
argument_list|,
literal|null
argument_list|,
literal|80001
argument_list|,
literal|11000
argument_list|)
block|}
decl_stmt|;
comment|// Now create the root and meta regions and insert the data regions
comment|// created above into the meta
name|createRootAndMetaRegions
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|regions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|HRegion
operator|.
name|addRegionToMETA
argument_list|(
name|meta
argument_list|,
name|regions
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|closeRootAndMeta
argument_list|()
expr_stmt|;
block|}
specifier|private
name|HRegion
name|createAregion
parameter_list|(
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
parameter_list|,
name|int
name|firstRow
parameter_list|,
name|int
name|nrows
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegion
name|region
init|=
name|createNewHRegion
argument_list|(
name|desc
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"created region "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HRegionIncommon
name|r
init|=
operator|new
name|HRegionIncommon
argument_list|(
name|region
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|firstRow
init|;
name|i
operator|<
name|firstRow
operator|+
name|nrows
condition|;
name|i
operator|++
control|)
block|{
name|BatchUpdate
name|batchUpdate
init|=
operator|new
name|BatchUpdate
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_"
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%1$05d"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|batchUpdate
operator|.
name|put
argument_list|(
name|COLUMN_NAME
argument_list|,
name|value
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|region
operator|.
name|batchUpdate
argument_list|(
name|batchUpdate
argument_list|,
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|%
literal|10000
operator|==
literal|0
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Flushing write #"
operator|+
name|i
argument_list|)
expr_stmt|;
name|r
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
block|}
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|region
operator|.
name|getLog
argument_list|()
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|setOffline
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|region
return|;
block|}
block|}
end_class

end_unit


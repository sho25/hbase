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

begin_comment
comment|/**  * Utility class to build a table of multiple regions.  */
end_comment

begin_class
specifier|public
class|class
name|MultiRegionTable
extends|extends
name|HBaseClusterTestCase
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|KEYS
init|=
block|{
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ddd"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"eee"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fff"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ggg"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"hhh"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"iii"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"jjj"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"kkk"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"lll"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"mmm"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"nnn"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ooo"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ppp"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qqq"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rrr"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"sss"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ttt"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"uuu"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"vvv"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"www"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"xxx"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"yyy"
argument_list|)
block|}
decl_stmt|;
specifier|protected
specifier|final
name|byte
index|[]
name|columnFamily
decl_stmt|;
specifier|protected
name|HTableDescriptor
name|desc
decl_stmt|;
comment|/**    * @param columnName the column to populate.    */
specifier|public
name|MultiRegionTable
parameter_list|(
specifier|final
name|String
name|columnName
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|columnFamily
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|columnName
argument_list|)
expr_stmt|;
comment|// These are needed for the new and improved Map/Reduce framework
name|System
operator|.
name|setProperty
argument_list|(
literal|"hadoop.log.dir"
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"hadoop.log.dir"
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"mapred.output.dir"
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"hadoop.tmp.dir"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run after dfs is ready but before hbase cluster is started up.    */
annotation|@
name|Override
specifier|protected
name|void
name|preHBaseClusterSetup
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
comment|// Create a bunch of regions
name|HRegion
index|[]
name|regions
init|=
operator|new
name|HRegion
index|[
name|KEYS
operator|.
name|length
index|]
decl_stmt|;
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
name|int
name|j
init|=
operator|(
name|i
operator|+
literal|1
operator|)
operator|%
name|regions
operator|.
name|length
decl_stmt|;
name|regions
index|[
name|i
index|]
operator|=
name|createARegion
argument_list|(
name|KEYS
index|[
name|i
index|]
argument_list|,
name|KEYS
index|[
name|j
index|]
argument_list|)
expr_stmt|;
block|}
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
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|shutdownDfs
argument_list|(
name|dfsCluster
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|private
name|HRegion
name|createARegion
parameter_list|(
name|byte
index|[]
name|startKey
parameter_list|,
name|byte
index|[]
name|endKey
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
name|addContent
argument_list|(
name|region
argument_list|,
name|this
operator|.
name|columnFamily
argument_list|)
expr_stmt|;
name|closeRegionAndDeleteLog
argument_list|(
name|region
argument_list|)
expr_stmt|;
return|return
name|region
return|;
block|}
specifier|private
name|void
name|closeRegionAndDeleteLog
parameter_list|(
name|HRegion
name|region
parameter_list|)
throws|throws
name|IOException
block|{
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
block|}
block|}
end_class

end_unit


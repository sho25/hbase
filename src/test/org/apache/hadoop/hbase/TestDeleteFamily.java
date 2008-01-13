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
name|util
operator|.
name|Map
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
name|dfs
operator|.
name|MiniDFSCluster
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
name|Text
import|;
end_import

begin_comment
comment|/**  * Test the functionality of deleteFamily.  */
end_comment

begin_class
specifier|public
class|class
name|TestDeleteFamily
extends|extends
name|HBaseTestCase
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
name|TestDeleteFamily
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|MiniDFSCluster
name|miniHdfs
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|this
operator|.
name|miniHdfs
operator|=
operator|new
name|MiniDFSCluster
argument_list|(
name|this
operator|.
name|conf
argument_list|,
literal|1
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests for HADOOP-2384.    * @throws Exception    */
specifier|public
name|void
name|testDeleteFamily
parameter_list|()
throws|throws
name|Exception
block|{
name|HRegion
name|region
init|=
literal|null
decl_stmt|;
name|HRegionIncommon
name|region_incommon
init|=
literal|null
decl_stmt|;
try|try
block|{
name|HTableDescriptor
name|htd
init|=
name|createTableDescriptor
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|region
operator|=
name|createNewHRegion
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|region_incommon
operator|=
operator|new
name|HRegionIncommon
argument_list|(
name|region
argument_list|)
expr_stmt|;
comment|// test memcache
name|makeSureItWorks
argument_list|(
name|region
argument_list|,
name|region_incommon
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// test hstore
name|makeSureItWorks
argument_list|(
name|region
argument_list|,
name|region_incommon
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
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
block|}
specifier|private
name|void
name|makeSureItWorks
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|HRegionIncommon
name|region_incommon
parameter_list|,
name|boolean
name|flush
parameter_list|)
throws|throws
name|Exception
block|{
comment|// insert a few versions worth of data for a row
name|Text
name|row
init|=
operator|new
name|Text
argument_list|(
literal|"test_row"
argument_list|)
decl_stmt|;
name|long
name|t0
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|t1
init|=
name|t0
operator|-
literal|15000
decl_stmt|;
name|long
name|t2
init|=
name|t1
operator|-
literal|15000
decl_stmt|;
name|Text
name|colA
init|=
operator|new
name|Text
argument_list|(
name|COLUMNS
index|[
literal|0
index|]
operator|.
name|toString
argument_list|()
operator|+
literal|"a"
argument_list|)
decl_stmt|;
name|Text
name|colB
init|=
operator|new
name|Text
argument_list|(
name|COLUMNS
index|[
literal|0
index|]
operator|.
name|toString
argument_list|()
operator|+
literal|"b"
argument_list|)
decl_stmt|;
name|Text
name|colC
init|=
operator|new
name|Text
argument_list|(
name|COLUMNS
index|[
literal|1
index|]
operator|.
name|toString
argument_list|()
operator|+
literal|"c"
argument_list|)
decl_stmt|;
name|long
name|lock
init|=
name|region_incommon
operator|.
name|startUpdate
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|region_incommon
operator|.
name|put
argument_list|(
name|lock
argument_list|,
name|colA
argument_list|,
name|cellData
argument_list|(
literal|0
argument_list|,
name|flush
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|put
argument_list|(
name|lock
argument_list|,
name|colB
argument_list|,
name|cellData
argument_list|(
literal|0
argument_list|,
name|flush
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|put
argument_list|(
name|lock
argument_list|,
name|colC
argument_list|,
name|cellData
argument_list|(
literal|0
argument_list|,
name|flush
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|commit
argument_list|(
name|lock
argument_list|,
name|t0
argument_list|)
expr_stmt|;
name|lock
operator|=
name|region_incommon
operator|.
name|startUpdate
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|put
argument_list|(
name|lock
argument_list|,
name|colA
argument_list|,
name|cellData
argument_list|(
literal|1
argument_list|,
name|flush
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|put
argument_list|(
name|lock
argument_list|,
name|colB
argument_list|,
name|cellData
argument_list|(
literal|1
argument_list|,
name|flush
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|put
argument_list|(
name|lock
argument_list|,
name|colC
argument_list|,
name|cellData
argument_list|(
literal|1
argument_list|,
name|flush
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|commit
argument_list|(
name|lock
argument_list|,
name|t1
argument_list|)
expr_stmt|;
name|lock
operator|=
name|region_incommon
operator|.
name|startUpdate
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|put
argument_list|(
name|lock
argument_list|,
name|colA
argument_list|,
name|cellData
argument_list|(
literal|2
argument_list|,
name|flush
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|put
argument_list|(
name|lock
argument_list|,
name|colB
argument_list|,
name|cellData
argument_list|(
literal|2
argument_list|,
name|flush
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|put
argument_list|(
name|lock
argument_list|,
name|colC
argument_list|,
name|cellData
argument_list|(
literal|2
argument_list|,
name|flush
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|commit
argument_list|(
name|lock
argument_list|,
name|t2
argument_list|)
expr_stmt|;
if|if
condition|(
name|flush
condition|)
block|{
name|region_incommon
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
comment|// call delete family at a timestamp, make sure only the most recent stuff
comment|// for column c is left behind
name|region
operator|.
name|deleteFamily
argument_list|(
name|row
argument_list|,
name|COLUMNS
index|[
literal|0
index|]
argument_list|,
name|t1
argument_list|)
expr_stmt|;
if|if
condition|(
name|flush
condition|)
block|{
name|region_incommon
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
comment|// most recent for A,B,C should be fine
comment|// A,B at older timestamps should be gone
comment|// C should be fine for older timestamps
name|assertCellValueEquals
argument_list|(
name|region
argument_list|,
name|row
argument_list|,
name|colA
argument_list|,
name|t0
argument_list|,
name|cellData
argument_list|(
literal|0
argument_list|,
name|flush
argument_list|)
argument_list|)
expr_stmt|;
name|assertCellValueEquals
argument_list|(
name|region
argument_list|,
name|row
argument_list|,
name|colA
argument_list|,
name|t1
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertCellValueEquals
argument_list|(
name|region
argument_list|,
name|row
argument_list|,
name|colA
argument_list|,
name|t2
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertCellValueEquals
argument_list|(
name|region
argument_list|,
name|row
argument_list|,
name|colB
argument_list|,
name|t0
argument_list|,
name|cellData
argument_list|(
literal|0
argument_list|,
name|flush
argument_list|)
argument_list|)
expr_stmt|;
name|assertCellValueEquals
argument_list|(
name|region
argument_list|,
name|row
argument_list|,
name|colB
argument_list|,
name|t1
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertCellValueEquals
argument_list|(
name|region
argument_list|,
name|row
argument_list|,
name|colB
argument_list|,
name|t2
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertCellValueEquals
argument_list|(
name|region
argument_list|,
name|row
argument_list|,
name|colC
argument_list|,
name|t0
argument_list|,
name|cellData
argument_list|(
literal|0
argument_list|,
name|flush
argument_list|)
argument_list|)
expr_stmt|;
name|assertCellValueEquals
argument_list|(
name|region
argument_list|,
name|row
argument_list|,
name|colC
argument_list|,
name|t1
argument_list|,
name|cellData
argument_list|(
literal|1
argument_list|,
name|flush
argument_list|)
argument_list|)
expr_stmt|;
name|assertCellValueEquals
argument_list|(
name|region
argument_list|,
name|row
argument_list|,
name|colC
argument_list|,
name|t2
argument_list|,
name|cellData
argument_list|(
literal|2
argument_list|,
name|flush
argument_list|)
argument_list|)
expr_stmt|;
comment|// call delete family w/o a timestamp, make sure nothing is left except for
comment|// column C.
name|region
operator|.
name|deleteFamily
argument_list|(
name|row
argument_list|,
name|COLUMNS
index|[
literal|0
index|]
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
if|if
condition|(
name|flush
condition|)
block|{
name|region_incommon
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
comment|// A,B for latest timestamp should be gone
comment|// C should still be fine
name|assertCellValueEquals
argument_list|(
name|region
argument_list|,
name|row
argument_list|,
name|colA
argument_list|,
name|t0
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertCellValueEquals
argument_list|(
name|region
argument_list|,
name|row
argument_list|,
name|colB
argument_list|,
name|t0
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertCellValueEquals
argument_list|(
name|region
argument_list|,
name|row
argument_list|,
name|colC
argument_list|,
name|t0
argument_list|,
name|cellData
argument_list|(
literal|0
argument_list|,
name|flush
argument_list|)
argument_list|)
expr_stmt|;
name|assertCellValueEquals
argument_list|(
name|region
argument_list|,
name|row
argument_list|,
name|colC
argument_list|,
name|t1
argument_list|,
name|cellData
argument_list|(
literal|1
argument_list|,
name|flush
argument_list|)
argument_list|)
expr_stmt|;
name|assertCellValueEquals
argument_list|(
name|region
argument_list|,
name|row
argument_list|,
name|colC
argument_list|,
name|t2
argument_list|,
name|cellData
argument_list|(
literal|2
argument_list|,
name|flush
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertCellValueEquals
parameter_list|(
specifier|final
name|HRegion
name|region
parameter_list|,
specifier|final
name|Text
name|row
parameter_list|,
specifier|final
name|Text
name|column
parameter_list|,
specifier|final
name|long
name|timestamp
parameter_list|,
specifier|final
name|String
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|result
init|=
name|region
operator|.
name|getFull
argument_list|(
name|row
argument_list|,
name|timestamp
argument_list|)
decl_stmt|;
name|byte
index|[]
name|cell_value
init|=
name|result
operator|.
name|get
argument_list|(
name|column
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|assertEquals
argument_list|(
name|column
operator|.
name|toString
argument_list|()
operator|+
literal|" at timestamp "
operator|+
name|timestamp
argument_list|,
literal|null
argument_list|,
name|cell_value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|cell_value
operator|==
literal|null
condition|)
block|{
name|fail
argument_list|(
name|column
operator|.
name|toString
argument_list|()
operator|+
literal|" at timestamp "
operator|+
name|timestamp
operator|+
literal|"\" was expected to be \""
operator|+
name|value
operator|+
literal|" but was null"
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|column
operator|.
name|toString
argument_list|()
operator|+
literal|" at timestamp "
operator|+
name|timestamp
argument_list|,
name|value
argument_list|,
operator|new
name|String
argument_list|(
name|cell_value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|cellData
parameter_list|(
name|int
name|tsNum
parameter_list|,
name|boolean
name|flush
parameter_list|)
block|{
return|return
literal|"t"
operator|+
name|tsNum
operator|+
literal|" data"
operator|+
operator|(
name|flush
condition|?
literal|" - with flush"
else|:
literal|""
operator|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|this
operator|.
name|miniHdfs
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|miniHdfs
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


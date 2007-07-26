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
name|UnsupportedEncodingException
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
name|java
operator|.
name|util
operator|.
name|TreeMap
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
comment|/**  * Test batch updates  */
end_comment

begin_class
specifier|public
class|class
name|TestBatchUpdate
extends|extends
name|HBaseClusterTestCase
block|{
specifier|private
specifier|static
specifier|final
name|String
name|CONTENTS_STR
init|=
literal|"contents:"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|CONTENTS
init|=
operator|new
name|Text
argument_list|(
name|CONTENTS_STR
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
name|value
decl_stmt|;
specifier|private
name|HTableDescriptor
name|desc
init|=
literal|null
decl_stmt|;
specifier|private
name|HClient
name|client
init|=
literal|null
decl_stmt|;
comment|/** constructor */
specifier|public
name|TestBatchUpdate
parameter_list|()
block|{
try|try
block|{
name|value
operator|=
literal|"abcd"
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
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
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
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
name|client
operator|=
operator|new
name|HClient
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|desc
operator|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|CONTENTS_STR
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|client
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|client
operator|.
name|openTable
argument_list|(
name|desc
operator|.
name|getName
argument_list|()
argument_list|)
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
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
comment|/** the test case */
specifier|public
name|void
name|testBatchUpdate
parameter_list|()
block|{
try|try
block|{
name|client
operator|.
name|commitBatch
argument_list|(
operator|-
literal|1L
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|e
parameter_list|)
block|{
comment|// expected
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
name|fail
argument_list|()
expr_stmt|;
block|}
name|long
name|lockid
init|=
name|client
operator|.
name|startBatchUpdate
argument_list|(
operator|new
name|Text
argument_list|(
literal|"row1"
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|client
operator|.
name|openTable
argument_list|(
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|e
parameter_list|)
block|{
comment|// expected
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
name|fail
argument_list|()
expr_stmt|;
block|}
try|try
block|{
try|try
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|long
name|dummy
init|=
name|client
operator|.
name|startUpdate
argument_list|(
operator|new
name|Text
argument_list|(
literal|"row2"
argument_list|)
argument_list|)
decl_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|e
parameter_list|)
block|{
comment|// expected
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
name|fail
argument_list|()
expr_stmt|;
block|}
name|client
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|CONTENTS
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|client
operator|.
name|delete
argument_list|(
name|lockid
argument_list|,
name|CONTENTS
argument_list|)
expr_stmt|;
name|client
operator|.
name|commitBatch
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
name|lockid
operator|=
name|client
operator|.
name|startBatchUpdate
argument_list|(
operator|new
name|Text
argument_list|(
literal|"row2"
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|CONTENTS
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|client
operator|.
name|commit
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
name|Text
index|[]
name|columns
init|=
block|{
name|CONTENTS
block|}
decl_stmt|;
name|HScannerInterface
name|scanner
init|=
name|client
operator|.
name|obtainScanner
argument_list|(
name|columns
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|)
decl_stmt|;
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|results
argument_list|)
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|e
range|:
name|results
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|key
operator|+
literal|": row: "
operator|+
name|e
operator|.
name|getKey
argument_list|()
operator|+
literal|" value: "
operator|+
operator|new
name|String
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
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
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


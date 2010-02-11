begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ArrayList
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
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Writables
import|;
end_import

begin_class
specifier|public
class|class
name|TestHMsg
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testList
parameter_list|()
block|{
name|List
argument_list|<
name|HMsg
argument_list|>
name|msgs
init|=
operator|new
name|ArrayList
argument_list|<
name|HMsg
argument_list|>
argument_list|()
decl_stmt|;
name|HMsg
name|hmsg
init|=
literal|null
decl_stmt|;
specifier|final
name|int
name|size
init|=
literal|10
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|b
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|hmsg
operator|=
operator|new
name|HMsg
argument_list|(
name|HMsg
operator|.
name|Type
operator|.
name|MSG_REGION_OPEN
argument_list|,
operator|new
name|HRegionInfo
argument_list|(
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
argument_list|,
name|b
argument_list|,
name|b
argument_list|)
argument_list|)
expr_stmt|;
name|msgs
operator|.
name|add
argument_list|(
name|hmsg
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|size
argument_list|,
name|msgs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|index
init|=
name|msgs
operator|.
name|indexOf
argument_list|(
name|hmsg
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
operator|-
literal|1
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|msgs
operator|.
name|remove
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|size
operator|-
literal|1
argument_list|,
name|msgs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|other
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"other"
argument_list|)
decl_stmt|;
name|hmsg
operator|=
operator|new
name|HMsg
argument_list|(
name|HMsg
operator|.
name|Type
operator|.
name|MSG_REGION_OPEN
argument_list|,
operator|new
name|HRegionInfo
argument_list|(
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
argument_list|,
name|other
argument_list|,
name|other
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|msgs
operator|.
name|indexOf
argument_list|(
name|hmsg
argument_list|)
argument_list|)
expr_stmt|;
comment|// Assert that two HMsgs are same if same content.
name|byte
index|[]
name|b
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|hmsg
operator|=
operator|new
name|HMsg
argument_list|(
name|HMsg
operator|.
name|Type
operator|.
name|MSG_REGION_OPEN
argument_list|,
operator|new
name|HRegionInfo
argument_list|(
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
argument_list|,
name|b
argument_list|,
name|b
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
operator|-
literal|1
argument_list|,
name|msgs
operator|.
name|indexOf
argument_list|(
name|hmsg
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Check out new HMsg that carries two daughter split regions.
name|byte
index|[]
name|abytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bbytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|parentbytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"parent"
argument_list|)
decl_stmt|;
name|HRegionInfo
name|parent
init|=
operator|new
name|HRegionInfo
argument_list|(
operator|new
name|HTableDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"parent"
argument_list|)
argument_list|)
argument_list|,
name|parentbytes
argument_list|,
name|parentbytes
argument_list|)
decl_stmt|;
comment|// Assert simple HMsg serializes
name|HMsg
name|hmsg
init|=
operator|new
name|HMsg
argument_list|(
name|HMsg
operator|.
name|Type
operator|.
name|MSG_REGION_CLOSE
argument_list|,
name|parent
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|Writables
operator|.
name|getBytes
argument_list|(
name|hmsg
argument_list|)
decl_stmt|;
name|HMsg
name|close
init|=
operator|(
name|HMsg
operator|)
name|Writables
operator|.
name|getWritable
argument_list|(
name|bytes
argument_list|,
operator|new
name|HMsg
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|close
operator|.
name|equals
argument_list|(
name|hmsg
argument_list|)
argument_list|)
expr_stmt|;
comment|// Assert split serializes
name|HRegionInfo
name|daughtera
init|=
operator|new
name|HRegionInfo
argument_list|(
operator|new
name|HTableDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|)
argument_list|,
name|abytes
argument_list|,
name|abytes
argument_list|)
decl_stmt|;
name|HRegionInfo
name|daughterb
init|=
operator|new
name|HRegionInfo
argument_list|(
operator|new
name|HTableDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
argument_list|)
argument_list|,
name|bbytes
argument_list|,
name|bbytes
argument_list|)
decl_stmt|;
name|HMsg
name|splithmsg
init|=
operator|new
name|HMsg
argument_list|(
name|HMsg
operator|.
name|Type
operator|.
name|MSG_REPORT_SPLIT_INCLUDES_DAUGHTERS
argument_list|,
name|parent
argument_list|,
name|daughtera
argument_list|,
name|daughterb
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"split"
argument_list|)
argument_list|)
decl_stmt|;
name|bytes
operator|=
name|Writables
operator|.
name|getBytes
argument_list|(
name|splithmsg
argument_list|)
expr_stmt|;
name|hmsg
operator|=
operator|(
name|HMsg
operator|)
name|Writables
operator|.
name|getWritable
argument_list|(
name|bytes
argument_list|,
operator|new
name|HMsg
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|splithmsg
operator|.
name|equals
argument_list|(
name|hmsg
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


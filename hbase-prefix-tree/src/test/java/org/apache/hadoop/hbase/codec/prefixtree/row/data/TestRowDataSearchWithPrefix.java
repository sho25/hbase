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
name|codec
operator|.
name|prefixtree
operator|.
name|row
operator|.
name|data
package|;
end_package

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
name|codec
operator|.
name|prefixtree
operator|.
name|row
operator|.
name|BaseTestRowData
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_class
specifier|public
class|class
name|TestRowDataSearchWithPrefix
extends|extends
name|BaseTestRowData
block|{
specifier|static
name|byte
index|[]
name|cf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
specifier|static
name|byte
index|[]
name|cq
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq"
argument_list|)
decl_stmt|;
specifier|static
name|byte
index|[]
name|v
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"v"
argument_list|)
decl_stmt|;
specifier|static
name|List
argument_list|<
name|KeyValue
argument_list|>
name|d
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
specifier|static
name|long
name|ts
init|=
literal|55L
decl_stmt|;
specifier|static
name|byte
index|[]
name|createRowKey
parameter_list|(
name|int
name|keyPart1
parameter_list|,
name|int
name|keyPart2
parameter_list|)
block|{
name|ByteArrayOutputStream
name|bos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|(
literal|16
argument_list|)
decl_stmt|;
name|DataOutputStream
name|dos
init|=
operator|new
name|DataOutputStream
argument_list|(
name|bos
argument_list|)
decl_stmt|;
try|try
block|{
name|dos
operator|.
name|writeInt
argument_list|(
name|keyPart1
argument_list|)
expr_stmt|;
name|dos
operator|.
name|writeInt
argument_list|(
name|keyPart2
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// should not happen
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|bos
operator|.
name|toByteArray
argument_list|()
return|;
block|}
static|static
block|{
name|d
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|createRowKey
argument_list|(
literal|1
argument_list|,
literal|12345
argument_list|)
argument_list|,
name|cf
argument_list|,
name|cq
argument_list|,
name|ts
argument_list|,
name|v
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|createRowKey
argument_list|(
literal|12345
argument_list|,
literal|0x01000000
argument_list|)
argument_list|,
name|cf
argument_list|,
name|cq
argument_list|,
name|ts
argument_list|,
name|v
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|createRowKey
argument_list|(
literal|12345
argument_list|,
literal|0x01010000
argument_list|)
argument_list|,
name|cf
argument_list|,
name|cq
argument_list|,
name|ts
argument_list|,
name|v
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|createRowKey
argument_list|(
literal|12345
argument_list|,
literal|0x02000000
argument_list|)
argument_list|,
name|cf
argument_list|,
name|cq
argument_list|,
name|ts
argument_list|,
name|v
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|createRowKey
argument_list|(
literal|12345
argument_list|,
literal|0x02020000
argument_list|)
argument_list|,
name|cf
argument_list|,
name|cq
argument_list|,
name|ts
argument_list|,
name|v
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|createRowKey
argument_list|(
literal|12345
argument_list|,
literal|0x03000000
argument_list|)
argument_list|,
name|cf
argument_list|,
name|cq
argument_list|,
name|ts
argument_list|,
name|v
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|createRowKey
argument_list|(
literal|12345
argument_list|,
literal|0x03030000
argument_list|)
argument_list|,
name|cf
argument_list|,
name|cq
argument_list|,
name|ts
argument_list|,
name|v
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|createRowKey
argument_list|(
literal|12345
argument_list|,
literal|0x04000000
argument_list|)
argument_list|,
name|cf
argument_list|,
name|cq
argument_list|,
name|ts
argument_list|,
name|v
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|createRowKey
argument_list|(
literal|12345
argument_list|,
literal|0x04040000
argument_list|)
argument_list|,
name|cf
argument_list|,
name|cq
argument_list|,
name|ts
argument_list|,
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|KeyValue
argument_list|>
name|getInputs
parameter_list|()
block|{
return|return
name|d
return|;
block|}
block|}
end_class

end_unit


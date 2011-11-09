begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
package|;
end_package

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
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

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
name|HBaseTestCase
operator|.
name|assertByteEquals
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
name|List
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
name|NavigableMap
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestResult
extends|extends
name|TestCase
block|{
specifier|static
name|KeyValue
index|[]
name|genKVs
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|,
specifier|final
name|long
name|timestamp
parameter_list|,
specifier|final
name|int
name|cols
parameter_list|)
block|{
name|KeyValue
index|[]
name|kvs
init|=
operator|new
name|KeyValue
index|[
name|cols
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
name|cols
condition|;
name|i
operator|++
control|)
block|{
name|kvs
index|[
name|i
index|]
operator|=
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|,
name|timestamp
argument_list|,
name|Bytes
operator|.
name|add
argument_list|(
name|value
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|kvs
return|;
block|}
specifier|static
specifier|final
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
specifier|public
name|void
name|testBasic
parameter_list|()
throws|throws
name|Exception
block|{
name|KeyValue
index|[]
name|kvs
init|=
name|genKVs
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|value
argument_list|,
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|kvs
argument_list|,
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
operator|new
name|Result
argument_list|(
name|kvs
argument_list|)
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
literal|100
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|byte
index|[]
name|qf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|ks
init|=
name|r
operator|.
name|getColumn
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|ks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertByteEquals
argument_list|(
name|qf
argument_list|,
name|ks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getQualifier
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|r
operator|.
name|getColumnLatest
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
argument_list|)
expr_stmt|;
name|assertByteEquals
argument_list|(
name|Bytes
operator|.
name|add
argument_list|(
name|value
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|,
name|r
operator|.
name|getValue
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|r
operator|.
name|containsColumn
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testMultiVersion
parameter_list|()
throws|throws
name|Exception
block|{
name|KeyValue
index|[]
name|kvs1
init|=
name|genKVs
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|value
argument_list|,
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|KeyValue
index|[]
name|kvs2
init|=
name|genKVs
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|value
argument_list|,
literal|200
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|KeyValue
index|[]
name|kvs
init|=
operator|new
name|KeyValue
index|[
name|kvs1
operator|.
name|length
operator|+
name|kvs2
operator|.
name|length
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|kvs1
argument_list|,
literal|0
argument_list|,
name|kvs
argument_list|,
literal|0
argument_list|,
name|kvs1
operator|.
name|length
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|kvs2
argument_list|,
literal|0
argument_list|,
name|kvs
argument_list|,
name|kvs1
operator|.
name|length
argument_list|,
name|kvs2
operator|.
name|length
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|kvs
argument_list|,
name|KeyValue
operator|.
name|COMPARATOR
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
operator|new
name|Result
argument_list|(
name|kvs
argument_list|)
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
literal|100
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|byte
index|[]
name|qf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|ks
init|=
name|r
operator|.
name|getColumn
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|ks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertByteEquals
argument_list|(
name|qf
argument_list|,
name|ks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getQualifier
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|ks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|r
operator|.
name|getColumnLatest
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
argument_list|)
expr_stmt|;
name|assertByteEquals
argument_list|(
name|Bytes
operator|.
name|add
argument_list|(
name|value
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|,
name|r
operator|.
name|getValue
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|r
operator|.
name|containsColumn
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


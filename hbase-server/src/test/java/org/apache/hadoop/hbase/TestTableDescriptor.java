begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbase
operator|.
name|client
operator|.
name|Durability
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
name|exceptions
operator|.
name|DeserializationException
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

begin_comment
comment|/**  * Test setting values in the descriptor  */
end_comment

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
name|TestTableDescriptor
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestTableDescriptor
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testPb
parameter_list|()
throws|throws
name|DeserializationException
throws|,
name|IOException
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
specifier|final
name|int
name|v
init|=
literal|123
decl_stmt|;
name|htd
operator|.
name|setMaxFileSize
argument_list|(
name|v
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|ASYNC_WAL
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setReadOnly
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setRegionReplication
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|TableDescriptor
name|td
init|=
operator|new
name|TableDescriptor
argument_list|(
name|htd
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|td
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|TableDescriptor
name|deserializedTd
init|=
name|TableDescriptor
operator|.
name|parseFrom
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|td
argument_list|,
name|deserializedTd
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|td
operator|.
name|getHTableDescriptor
argument_list|()
argument_list|,
name|deserializedTd
operator|.
name|getHTableDescriptor
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|rest
operator|.
name|model
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|JAXBContext
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|JAXBException
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
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestRowModel
extends|extends
name|TestModelBase
argument_list|<
name|RowModel
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testrow1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testcolumn1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VALUE1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testvalue1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|TIMESTAMP1
init|=
literal|1245219839331L
decl_stmt|;
specifier|private
name|JAXBContext
name|context
decl_stmt|;
specifier|public
name|TestRowModel
parameter_list|()
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|RowModel
operator|.
name|class
argument_list|)
expr_stmt|;
name|AS_XML
operator|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Row key=\"dGVzdHJvdzE=\">"
operator|+
literal|"<Cell column=\"dGVzdGNvbHVtbjE=\" timestamp=\"1245219839331\">dGVzdHZhbHVlMQ==</Cell></Row>"
expr_stmt|;
name|AS_JSON
operator|=
literal|"{\"key\":\"dGVzdHJvdzE=\",\"Cell\":[{\"column\":\"dGVzdGNvbHVtbjE=\","
operator|+
literal|"\"timestamp\":1245219839331,\"$\":\"dGVzdHZhbHVlMQ==\"}]}"
expr_stmt|;
block|}
specifier|protected
name|RowModel
name|buildTestModel
parameter_list|()
block|{
name|RowModel
name|model
init|=
operator|new
name|RowModel
argument_list|()
decl_stmt|;
name|model
operator|.
name|setKey
argument_list|(
name|ROW1
argument_list|)
expr_stmt|;
name|model
operator|.
name|addCell
argument_list|(
operator|new
name|CellModel
argument_list|(
name|COLUMN1
argument_list|,
name|TIMESTAMP1
argument_list|,
name|VALUE1
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|model
return|;
block|}
specifier|protected
name|void
name|checkModel
parameter_list|(
name|RowModel
name|model
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|ROW1
argument_list|,
name|model
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|CellModel
argument_list|>
name|cells
init|=
name|model
operator|.
name|getCells
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|CellModel
name|cell
init|=
name|cells
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|COLUMN1
argument_list|,
name|cell
operator|.
name|getColumn
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|VALUE1
argument_list|,
name|cell
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cell
operator|.
name|hasUserTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|TIMESTAMP1
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|cells
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|testFromPB
parameter_list|()
throws|throws
name|Exception
block|{
comment|//do nothing row model has no PB
block|}
block|}
end_class

end_unit


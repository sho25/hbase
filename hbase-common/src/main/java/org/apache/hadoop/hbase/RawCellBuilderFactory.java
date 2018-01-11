begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Factory for creating cells for CPs. It does deep_copy {@link CellBuilderType#DEEP_COPY} while  * creating cells.  * This is private because coprocessors should get an instance of type {@link RawCellBuilder}  * using RegionCoprocessorEnvironment#getCellBuilder.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|RawCellBuilderFactory
block|{
comment|/**    * @return the cell that is created    */
specifier|public
specifier|static
name|RawCellBuilder
name|create
parameter_list|()
block|{
return|return
operator|new
name|KeyValueBuilder
argument_list|()
return|;
block|}
specifier|private
name|RawCellBuilderFactory
parameter_list|()
block|{   }
block|}
end_class

end_unit


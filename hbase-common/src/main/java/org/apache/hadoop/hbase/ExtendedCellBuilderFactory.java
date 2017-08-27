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
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|ExtendedCellBuilderFactory
block|{
specifier|public
specifier|static
name|ExtendedCellBuilder
name|create
parameter_list|(
name|CellBuilderType
name|type
parameter_list|)
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|SHALLOW_COPY
case|:
return|return
operator|new
name|IndividualBytesFieldCellBuilder
argument_list|()
return|;
case|case
name|DEEP_COPY
case|:
return|return
operator|new
name|KeyValueBuilder
argument_list|()
return|;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"The type:"
operator|+
name|type
operator|+
literal|" is unsupported"
argument_list|)
throw|;
block|}
block|}
specifier|private
name|ExtendedCellBuilderFactory
parameter_list|()
block|{   }
block|}
end_class

end_unit


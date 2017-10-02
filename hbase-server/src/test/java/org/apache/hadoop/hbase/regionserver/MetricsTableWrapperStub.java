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
operator|.
name|regionserver
package|;
end_package

begin_class
specifier|public
class|class
name|MetricsTableWrapperStub
implements|implements
name|MetricsTableWrapperAggregate
block|{
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|public
name|MetricsTableWrapperStub
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getReadRequestsCount
parameter_list|(
name|String
name|table
parameter_list|)
block|{
return|return
literal|10
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getWriteRequestsCount
parameter_list|(
name|String
name|table
parameter_list|)
block|{
return|return
literal|20
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getTotalRequestsCount
parameter_list|(
name|String
name|table
parameter_list|)
block|{
return|return
literal|30
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMemStoresSize
parameter_list|(
name|String
name|table
parameter_list|)
block|{
return|return
literal|1000
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getStoreFilesSize
parameter_list|(
name|String
name|table
parameter_list|)
block|{
return|return
literal|2000
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getTableSize
parameter_list|(
name|String
name|table
parameter_list|)
block|{
return|return
literal|3000
return|;
block|}
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
block|}
end_class

end_unit


begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
import|;
end_import

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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|EagerMemStoreCompactionStrategy
extends|extends
name|MemStoreCompactionStrategy
block|{
specifier|private
specifier|static
specifier|final
name|String
name|name
init|=
literal|"EAGER"
decl_stmt|;
specifier|public
name|EagerMemStoreCompactionStrategy
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|cfName
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|cfName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Action
name|getAction
parameter_list|(
name|VersionedSegmentsList
name|versionedList
parameter_list|)
block|{
return|return
name|compact
argument_list|(
name|versionedList
argument_list|,
name|name
argument_list|)
return|;
block|}
block|}
end_class

end_unit

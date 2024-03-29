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
name|tool
operator|.
name|coprocessor
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|coprocessor
operator|.
name|BulkLoadObserver
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
name|coprocessor
operator|.
name|EndpointObserver
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
name|coprocessor
operator|.
name|MasterObserver
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
name|coprocessor
operator|.
name|RegionObserver
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
name|coprocessor
operator|.
name|RegionServerObserver
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
name|coprocessor
operator|.
name|WALObserver
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
name|CurrentCoprocessorMethods
extends|extends
name|CoprocessorMethods
block|{
specifier|public
name|CurrentCoprocessorMethods
parameter_list|()
block|{
name|addMethods
argument_list|(
name|BulkLoadObserver
operator|.
name|class
argument_list|)
expr_stmt|;
name|addMethods
argument_list|(
name|EndpointObserver
operator|.
name|class
argument_list|)
expr_stmt|;
name|addMethods
argument_list|(
name|MasterObserver
operator|.
name|class
argument_list|)
expr_stmt|;
name|addMethods
argument_list|(
name|RegionObserver
operator|.
name|class
argument_list|)
expr_stmt|;
name|addMethods
argument_list|(
name|RegionServerObserver
operator|.
name|class
argument_list|)
expr_stmt|;
name|addMethods
argument_list|(
name|WALObserver
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addMethods
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|)
block|{
for|for
control|(
name|Method
name|method
range|:
name|clazz
operator|.
name|getDeclaredMethods
argument_list|()
control|)
block|{
name|addMethod
argument_list|(
name|method
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


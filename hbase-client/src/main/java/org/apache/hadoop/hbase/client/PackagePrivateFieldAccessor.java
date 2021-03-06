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
name|client
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
comment|/**  * A helper class used to access the package private field in o.a.h.h.client package.  *<p>  * This is because we share some data structures between client and server and the data structures  * are marked as {@code InterfaceAudience.Public}, but we do not want to expose some of the fields  * to end user.  *<p>  * TODO: A better solution is to separate the data structures used in client and server.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|PackagePrivateFieldAccessor
block|{
specifier|public
specifier|static
name|void
name|setMvccReadPoint
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|long
name|mvccReadPoint
parameter_list|)
block|{
name|scan
operator|.
name|setMvccReadPoint
argument_list|(
name|mvccReadPoint
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|long
name|getMvccReadPoint
parameter_list|(
name|Scan
name|scan
parameter_list|)
block|{
return|return
name|scan
operator|.
name|getMvccReadPoint
argument_list|()
return|;
block|}
block|}
end_class

end_unit


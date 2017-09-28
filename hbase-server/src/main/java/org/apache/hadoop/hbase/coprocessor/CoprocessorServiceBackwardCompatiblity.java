begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|coprocessor
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Service
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
import|;
end_import

begin_comment
comment|/**  * Classes to help maintain backward compatibility with now deprecated {@link CoprocessorService}  * and {@link SingletonCoprocessorService}.  * From 2.0 onwards, implementors of coprocessor service should also implement the relevant  * coprocessor class (For eg {@link MasterCoprocessor} for coprocessor service in master), and  * override get*Service() method to return the {@link com.google.protobuf.Service} object.  * To maintain backward compatibility with 1.0 implementation, we'll wrap implementation of  * CoprocessorService/SingletonCoprocessorService in the new  * {Master, Region, RegionServer}Coprocessor class.  * Since there is no backward compatibility guarantee for Observers, we leave get*Observer() to  * default which returns null.  * This approach to maintain backward compatibility seems cleaner and more explicit.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|Deprecated
specifier|public
class|class
name|CoprocessorServiceBackwardCompatiblity
block|{
specifier|static
specifier|public
class|class
name|MasterCoprocessorService
implements|implements
name|MasterCoprocessor
block|{
name|CoprocessorService
name|service
decl_stmt|;
specifier|public
name|MasterCoprocessorService
parameter_list|(
name|CoprocessorService
name|service
parameter_list|)
block|{
name|this
operator|.
name|service
operator|=
name|service
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|Service
argument_list|>
name|getService
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|service
operator|.
name|getService
argument_list|()
argument_list|)
return|;
block|}
block|}
specifier|static
specifier|public
class|class
name|RegionCoprocessorService
implements|implements
name|RegionCoprocessor
block|{
name|CoprocessorService
name|service
decl_stmt|;
specifier|public
name|RegionCoprocessorService
parameter_list|(
name|CoprocessorService
name|service
parameter_list|)
block|{
name|this
operator|.
name|service
operator|=
name|service
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|Service
argument_list|>
name|getService
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|service
operator|.
name|getService
argument_list|()
argument_list|)
return|;
block|}
block|}
specifier|static
specifier|public
class|class
name|RegionServerCoprocessorService
implements|implements
name|RegionServerCoprocessor
block|{
name|SingletonCoprocessorService
name|service
decl_stmt|;
specifier|public
name|RegionServerCoprocessorService
parameter_list|(
name|SingletonCoprocessorService
name|service
parameter_list|)
block|{
name|this
operator|.
name|service
operator|=
name|service
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|Service
argument_list|>
name|getService
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|service
operator|.
name|getService
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

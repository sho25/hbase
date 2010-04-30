begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|master
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_comment
comment|/**  * Test the queue used to manage RegionServerOperations.  * Currently RegionServerOperationQueue is untestable because each  * RegionServerOperation has a {@link HMaster} reference.  TOOD: Fix.  */
end_comment

begin_class
specifier|public
class|class
name|TestRegionServerOperationQueue
block|{
specifier|private
name|RegionServerOperationQueue
name|queue
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|AtomicBoolean
name|closed
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|closed
operator|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
name|this
operator|.
name|queue
operator|=
operator|new
name|RegionServerOperationQueue
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|this
operator|.
name|closed
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{   }
block|}
end_class

end_unit


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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Keeps track of repeated failures to any region server. Multiple threads manipulate the contents  * of this thread.  *  * Access to the members is guarded by the concurrent nature of the members inherently.  *   */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|FailureInfo
block|{
comment|// The number of consecutive failures.
specifier|public
specifier|final
name|AtomicLong
name|numConsecutiveFailures
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
comment|// The time when the server started to become unresponsive
comment|// Once set, this would never be updated.
specifier|public
specifier|final
name|long
name|timeOfFirstFailureMilliSec
decl_stmt|;
comment|// The time when the client last tried to contact the server.
comment|// This is only updated by one client at a time
specifier|public
specifier|volatile
name|long
name|timeOfLatestAttemptMilliSec
decl_stmt|;
comment|// Used to keep track of concurrent attempts to contact the server.
comment|// In Fast fail mode, we want just one client thread to try to connect
comment|// the rest of the client threads will fail fast.
specifier|public
specifier|final
name|AtomicBoolean
name|exclusivelyRetringInspiteOfFastFail
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"FailureInfo: numConsecutiveFailures = "
operator|+
name|numConsecutiveFailures
operator|+
literal|" timeOfFirstFailureMilliSec = "
operator|+
name|timeOfFirstFailureMilliSec
operator|+
literal|" timeOfLatestAttemptMilliSec = "
operator|+
name|timeOfLatestAttemptMilliSec
operator|+
literal|" exclusivelyRetringInspiteOfFastFail  = "
operator|+
name|exclusivelyRetringInspiteOfFastFail
operator|.
name|get
argument_list|()
return|;
block|}
name|FailureInfo
parameter_list|(
name|long
name|firstFailureTime
parameter_list|)
block|{
name|this
operator|.
name|timeOfFirstFailureMilliSec
operator|=
name|firstFailureTime
expr_stmt|;
block|}
block|}
end_class

end_unit


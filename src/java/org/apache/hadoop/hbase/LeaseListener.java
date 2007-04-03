begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/*******************************************************************************  * LeaseListener is a small class meant to be overridden by users of the Leases   * class.  *  * It receives events from the Leases class about the status of its accompanying  * lease.  Users of the Leases class can use a LeaseListener subclass to, for   * example, clean up resources after a lease has expired.  ******************************************************************************/
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|LeaseListener
block|{
specifier|public
name|LeaseListener
parameter_list|()
block|{   }
specifier|public
name|void
name|leaseRenewed
parameter_list|()
block|{   }
comment|/** When the user cancels a lease, this method is called. */
specifier|public
name|void
name|leaseCancelled
parameter_list|()
block|{   }
comment|/** When a lease expires, this method is called. */
specifier|public
name|void
name|leaseExpired
parameter_list|()
block|{   }
block|}
end_class

end_unit


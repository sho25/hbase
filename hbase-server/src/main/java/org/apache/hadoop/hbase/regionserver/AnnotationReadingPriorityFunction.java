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
name|regionserver
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
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HConstants
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
name|TableName
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
name|ipc
operator|.
name|PriorityFunction
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
name|ipc
operator|.
name|QosPriority
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|RegionServerStatusProtos
operator|.
name|ReportRegionStateTransitionRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|RegionServerStatusProtos
operator|.
name|RegionStateTransition
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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|CloseRegionRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|CompactRegionRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|FlushRegionRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|GetRegionInfoRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|GetStoreFileRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|SplitRegionRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|GetRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|MultiRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|MutateRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ScanRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|RegionSpecifier
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
name|protobuf
operator|.
name|generated
operator|.
name|RPCProtos
operator|.
name|RequestHeader
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|TextFormat
import|;
end_import

begin_comment
comment|/**  * Reads special method annotations and table names to figure a priority for use by QoS facility in  * ipc; e.g: rpcs to hbase:meta get priority.  */
end_comment

begin_comment
comment|// TODO: Remove.  This is doing way too much work just to figure a priority.  Do as Elliott
end_comment

begin_comment
comment|// suggests and just have the client specify a priority.
end_comment

begin_comment
comment|//The logic for figuring out high priority RPCs is as follows:
end_comment

begin_comment
comment|//1. if the method is annotated with a QosPriority of QOS_HIGH,
end_comment

begin_comment
comment|//   that is honored
end_comment

begin_comment
comment|//2. parse out the protobuf message and see if the request is for meta
end_comment

begin_comment
comment|//   region, and if so, treat it as a high priority RPC
end_comment

begin_comment
comment|//Some optimizations for (2) are done here -
end_comment

begin_comment
comment|//Clients send the argument classname as part of making the RPC. The server
end_comment

begin_comment
comment|//decides whether to deserialize the proto argument message based on the
end_comment

begin_comment
comment|//pre-established set of argument classes (knownArgumentClasses below).
end_comment

begin_comment
comment|//This prevents the server from having to deserialize all proto argument
end_comment

begin_comment
comment|//messages prematurely.
end_comment

begin_comment
comment|//All the argument classes declare a 'getRegion' method that returns a
end_comment

begin_comment
comment|//RegionSpecifier object. Methods can be invoked on the returned object
end_comment

begin_comment
comment|//to figure out whether it is a meta region or not.
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|AnnotationReadingPriorityFunction
implements|implements
name|PriorityFunction
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|AnnotationReadingPriorityFunction
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/** Used to control the scan delay, currently sqrt(numNextCall * weight) */
specifier|public
specifier|static
specifier|final
name|String
name|SCAN_VTIME_WEIGHT_CONF_KEY
init|=
literal|"hbase.ipc.server.scan.vtime.weight"
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|annotatedQos
decl_stmt|;
comment|//We need to mock the regionserver instance for some unit tests (set via
comment|//setRegionServer method.
specifier|private
name|RSRpcServices
name|rpcServices
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Message
argument_list|>
index|[]
name|knownArgumentClasses
init|=
operator|new
name|Class
index|[]
block|{
name|GetRegionInfoRequest
operator|.
name|class
block|,
name|GetStoreFileRequest
operator|.
name|class
block|,
name|CloseRegionRequest
operator|.
name|class
block|,
name|FlushRegionRequest
operator|.
name|class
block|,
name|SplitRegionRequest
operator|.
name|class
block|,
name|CompactRegionRequest
operator|.
name|class
block|,
name|GetRequest
operator|.
name|class
block|,
name|MutateRequest
operator|.
name|class
block|,
name|ScanRequest
operator|.
name|class
block|}
decl_stmt|;
comment|// Some caches for helping performance
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Message
argument_list|>
argument_list|>
name|argumentToClassMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Message
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Message
argument_list|>
argument_list|,
name|Method
argument_list|>
argument_list|>
name|methodMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Message
argument_list|>
argument_list|,
name|Method
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|float
name|scanVirtualTimeWeight
decl_stmt|;
comment|/**    * Calls {@link #AnnotationReadingPriorityFunction(RSRpcServices, Class)} using the result of    * {@code rpcServices#getClass()}    *    * @param rpcServices    *          The RPC server implementation    */
name|AnnotationReadingPriorityFunction
parameter_list|(
specifier|final
name|RSRpcServices
name|rpcServices
parameter_list|)
block|{
name|this
argument_list|(
name|rpcServices
argument_list|,
name|rpcServices
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructs the priority function given the RPC server implementation and the annotations on the    * methods in the provided {@code clz}.    *    * @param rpcServices    *          The RPC server implementation    * @param clz    *          The concrete RPC server implementation's class    */
name|AnnotationReadingPriorityFunction
parameter_list|(
specifier|final
name|RSRpcServices
name|rpcServices
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|RSRpcServices
argument_list|>
name|clz
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|qosMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Method
name|m
range|:
name|clz
operator|.
name|getMethods
argument_list|()
control|)
block|{
name|QosPriority
name|p
init|=
name|m
operator|.
name|getAnnotation
argument_list|(
name|QosPriority
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|p
operator|!=
literal|null
condition|)
block|{
comment|// Since we protobuf'd, and then subsequently, when we went with pb style, method names
comment|// are capitalized.  This meant that this brittle compare of method names gotten by
comment|// reflection no longer matched the method names coming in over pb.  TODO: Get rid of this
comment|// check.  For now, workaround is to capitalize the names we got from reflection so they
comment|// have chance of matching the pb ones.
name|String
name|capitalizedMethodName
init|=
name|capitalize
argument_list|(
name|m
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|qosMap
operator|.
name|put
argument_list|(
name|capitalizedMethodName
argument_list|,
name|p
operator|.
name|priority
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|rpcServices
operator|=
name|rpcServices
expr_stmt|;
name|this
operator|.
name|annotatedQos
operator|=
name|qosMap
expr_stmt|;
if|if
condition|(
name|methodMap
operator|.
name|get
argument_list|(
literal|"getRegion"
argument_list|)
operator|==
literal|null
condition|)
block|{
name|methodMap
operator|.
name|put
argument_list|(
literal|"hasRegion"
argument_list|,
operator|new
name|HashMap
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Message
argument_list|>
argument_list|,
name|Method
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|methodMap
operator|.
name|put
argument_list|(
literal|"getRegion"
argument_list|,
operator|new
name|HashMap
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Message
argument_list|>
argument_list|,
name|Method
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|Message
argument_list|>
name|cls
range|:
name|knownArgumentClasses
control|)
block|{
name|argumentToClassMap
operator|.
name|put
argument_list|(
name|cls
operator|.
name|getName
argument_list|()
argument_list|,
name|cls
argument_list|)
expr_stmt|;
try|try
block|{
name|methodMap
operator|.
name|get
argument_list|(
literal|"hasRegion"
argument_list|)
operator|.
name|put
argument_list|(
name|cls
argument_list|,
name|cls
operator|.
name|getDeclaredMethod
argument_list|(
literal|"hasRegion"
argument_list|)
argument_list|)
expr_stmt|;
name|methodMap
operator|.
name|get
argument_list|(
literal|"getRegion"
argument_list|)
operator|.
name|put
argument_list|(
name|cls
argument_list|,
name|cls
operator|.
name|getDeclaredMethod
argument_list|(
literal|"getRegion"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
name|Configuration
name|conf
init|=
name|rpcServices
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|scanVirtualTimeWeight
operator|=
name|conf
operator|.
name|getFloat
argument_list|(
name|SCAN_VTIME_WEIGHT_CONF_KEY
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|capitalize
parameter_list|(
specifier|final
name|String
name|s
parameter_list|)
block|{
name|StringBuilder
name|strBuilder
init|=
operator|new
name|StringBuilder
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|strBuilder
operator|.
name|setCharAt
argument_list|(
literal|0
argument_list|,
name|Character
operator|.
name|toUpperCase
argument_list|(
name|strBuilder
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|strBuilder
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Returns a 'priority' based on the request type.    *    * Currently the returned priority is used for queue selection.    * See the SimpleRpcScheduler as example. It maintains a queue per 'priory type'    * HIGH_QOS (meta requests), REPLICATION_QOS (replication requests),    * NORMAL_QOS (user requests).    */
annotation|@
name|Override
specifier|public
name|int
name|getPriority
parameter_list|(
name|RequestHeader
name|header
parameter_list|,
name|Message
name|param
parameter_list|)
block|{
name|String
name|methodName
init|=
name|header
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
name|Integer
name|priorityByAnnotation
init|=
name|annotatedQos
operator|.
name|get
argument_list|(
name|methodName
argument_list|)
decl_stmt|;
if|if
condition|(
name|priorityByAnnotation
operator|!=
literal|null
condition|)
block|{
return|return
name|priorityByAnnotation
return|;
block|}
if|if
condition|(
name|param
operator|==
literal|null
condition|)
block|{
return|return
name|HConstants
operator|.
name|NORMAL_QOS
return|;
block|}
if|if
condition|(
name|methodName
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"multi"
argument_list|)
operator|&&
name|param
operator|instanceof
name|MultiRequest
condition|)
block|{
comment|// The multi call has its priority set in the header.  All calls should work this way but
comment|// only this one has been converted so far.  No priority == NORMAL_QOS.
return|return
name|header
operator|.
name|hasPriority
argument_list|()
condition|?
name|header
operator|.
name|getPriority
argument_list|()
else|:
name|HConstants
operator|.
name|NORMAL_QOS
return|;
block|}
name|String
name|cls
init|=
name|param
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|Message
argument_list|>
name|rpcArgClass
init|=
name|argumentToClassMap
operator|.
name|get
argument_list|(
name|cls
argument_list|)
decl_stmt|;
name|RegionSpecifier
name|regionSpecifier
init|=
literal|null
decl_stmt|;
comment|//check whether the request has reference to meta region or now.
try|try
block|{
comment|// Check if the param has a region specifier; the pb methods are hasRegion and getRegion if
comment|// hasRegion returns true.  Not all listed methods have region specifier each time.  For
comment|// example, the ScanRequest has it on setup but thereafter relies on the scannerid rather than
comment|// send the region over every time.
name|Method
name|hasRegion
init|=
name|methodMap
operator|.
name|get
argument_list|(
literal|"hasRegion"
argument_list|)
operator|.
name|get
argument_list|(
name|rpcArgClass
argument_list|)
decl_stmt|;
if|if
condition|(
name|hasRegion
operator|!=
literal|null
operator|&&
operator|(
name|Boolean
operator|)
name|hasRegion
operator|.
name|invoke
argument_list|(
name|param
argument_list|,
operator|(
name|Object
index|[]
operator|)
literal|null
argument_list|)
condition|)
block|{
name|Method
name|getRegion
init|=
name|methodMap
operator|.
name|get
argument_list|(
literal|"getRegion"
argument_list|)
operator|.
name|get
argument_list|(
name|rpcArgClass
argument_list|)
decl_stmt|;
name|regionSpecifier
operator|=
operator|(
name|RegionSpecifier
operator|)
name|getRegion
operator|.
name|invoke
argument_list|(
name|param
argument_list|,
operator|(
name|Object
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
name|Region
name|region
init|=
name|rpcServices
operator|.
name|getRegion
argument_list|(
name|regionSpecifier
argument_list|)
decl_stmt|;
if|if
condition|(
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isSystemTable
argument_list|()
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"High priority because region="
operator|+
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|HConstants
operator|.
name|SYSTEMTABLE_QOS
return|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
comment|// Not good throwing an exception out of here, a runtime anyways.  Let the query go into the
comment|// server and have it throw the exception if still an issue.  Just mark it normal priority.
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|trace
argument_list|(
literal|"Marking normal priority after getting exception="
operator|+
name|ex
argument_list|)
expr_stmt|;
return|return
name|HConstants
operator|.
name|NORMAL_QOS
return|;
block|}
if|if
condition|(
name|methodName
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"scan"
argument_list|)
condition|)
block|{
comment|// scanner methods...
name|ScanRequest
name|request
init|=
operator|(
name|ScanRequest
operator|)
name|param
decl_stmt|;
if|if
condition|(
operator|!
name|request
operator|.
name|hasScannerId
argument_list|()
condition|)
block|{
return|return
name|HConstants
operator|.
name|NORMAL_QOS
return|;
block|}
name|RegionScanner
name|scanner
init|=
name|rpcServices
operator|.
name|getScanner
argument_list|(
name|request
operator|.
name|getScannerId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|scanner
operator|!=
literal|null
operator|&&
name|scanner
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isSystemTable
argument_list|()
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
comment|// Scanner requests are small in size so TextFormat version should not overwhelm log.
name|LOG
operator|.
name|trace
argument_list|(
literal|"High priority scanner request "
operator|+
name|TextFormat
operator|.
name|shortDebugString
argument_list|(
name|request
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|HConstants
operator|.
name|SYSTEMTABLE_QOS
return|;
block|}
block|}
comment|// If meta is moving then all the rest of report the report state transitions will be
comment|// blocked. We shouldn't be in the same queue.
if|if
condition|(
name|methodName
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"ReportRegionStateTransition"
argument_list|)
condition|)
block|{
comment|// Regions are moving
name|ReportRegionStateTransitionRequest
name|tRequest
init|=
operator|(
name|ReportRegionStateTransitionRequest
operator|)
name|param
decl_stmt|;
for|for
control|(
name|RegionStateTransition
name|transition
range|:
name|tRequest
operator|.
name|getTransitionList
argument_list|()
control|)
block|{
if|if
condition|(
name|transition
operator|.
name|getRegionInfoList
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|HBaseProtos
operator|.
name|RegionInfo
name|info
range|:
name|transition
operator|.
name|getRegionInfoList
argument_list|()
control|)
block|{
name|TableName
name|tn
init|=
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|info
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|tn
operator|.
name|isSystemTable
argument_list|()
condition|)
block|{
return|return
name|HConstants
operator|.
name|SYSTEMTABLE_QOS
return|;
block|}
block|}
block|}
block|}
block|}
return|return
name|HConstants
operator|.
name|NORMAL_QOS
return|;
block|}
comment|/**    * Based on the request content, returns the deadline of the request.    *    * @param header    * @param param    * @return Deadline of this request. 0 now, otherwise msec of 'delay'    */
annotation|@
name|Override
specifier|public
name|long
name|getDeadline
parameter_list|(
name|RequestHeader
name|header
parameter_list|,
name|Message
name|param
parameter_list|)
block|{
name|String
name|methodName
init|=
name|header
operator|.
name|getMethodName
argument_list|()
decl_stmt|;
if|if
condition|(
name|methodName
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"scan"
argument_list|)
condition|)
block|{
name|ScanRequest
name|request
init|=
operator|(
name|ScanRequest
operator|)
name|param
decl_stmt|;
if|if
condition|(
operator|!
name|request
operator|.
name|hasScannerId
argument_list|()
condition|)
block|{
return|return
literal|0
return|;
block|}
comment|// get the 'virtual time' of the scanner, and applies sqrt() to get a
comment|// nice curve for the delay. More a scanner is used the less priority it gets.
comment|// The weight is used to have more control on the delay.
name|long
name|vtime
init|=
name|rpcServices
operator|.
name|getScannerVirtualTime
argument_list|(
name|request
operator|.
name|getScannerId
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|Math
operator|.
name|round
argument_list|(
name|Math
operator|.
name|sqrt
argument_list|(
name|vtime
operator|*
name|scanVirtualTimeWeight
argument_list|)
argument_list|)
return|;
block|}
return|return
literal|0
return|;
block|}
annotation|@
name|VisibleForTesting
name|void
name|setRegionServer
parameter_list|(
specifier|final
name|HRegionServer
name|hrs
parameter_list|)
block|{
name|this
operator|.
name|rpcServices
operator|=
name|hrs
operator|.
name|getRSRpcServices
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


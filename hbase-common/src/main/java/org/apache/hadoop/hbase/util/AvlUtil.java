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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|AtomicBoolean
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
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Helper class that allows to create and manipulate an AvlTree.  * The main utility is in cases where over time we have a lot of add/remove of the same object,  * and we want to avoid all the allocations/deallocations of the "node" objects that the  * java containers will create.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|final
class|class
name|AvlUtil
block|{
specifier|private
name|AvlUtil
parameter_list|()
block|{}
comment|/**    * This class represent a node that will be used in an AvlTree.    * Instead of creating another object for the tree node,    * like the TreeMap and the other java contains, here the node can be extended    * and the content can be embedded directly in the node itself.    * This is useful in cases where over time we have a lot of add/remove of the same object.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
specifier|abstract
class|class
name|AvlNode
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
block|{
specifier|protected
name|TNode
name|avlLeft
decl_stmt|;
specifier|protected
name|TNode
name|avlRight
decl_stmt|;
specifier|protected
name|int
name|avlHeight
decl_stmt|;
specifier|public
specifier|abstract
name|int
name|compareTo
parameter_list|(
name|TNode
name|other
parameter_list|)
function_decl|;
block|}
comment|/**    * This class extends the AvlNode and adds two links that will be used in conjunction    * with the AvlIterableList class.    * This is useful in situations where your node must be in a map to have a quick lookup by key,    * but it also require to be in something like a list/queue.    * This is useful in cases where over time we have a lot of add/remove of the same object.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
specifier|abstract
class|class
name|AvlLinkedNode
parameter_list|<
name|TNode
extends|extends
name|AvlLinkedNode
parameter_list|>
extends|extends
name|AvlNode
argument_list|<
name|TNode
argument_list|>
block|{
specifier|protected
name|TNode
name|iterNext
init|=
literal|null
decl_stmt|;
specifier|protected
name|TNode
name|iterPrev
init|=
literal|null
decl_stmt|;
block|}
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|AvlInsertOrReplace
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
block|{
name|TNode
name|insert
parameter_list|(
name|Object
name|searchKey
parameter_list|)
function_decl|;
name|TNode
name|replace
parameter_list|(
name|Object
name|searchKey
parameter_list|,
name|TNode
name|prevNode
parameter_list|)
function_decl|;
block|}
comment|/**    * The AvlTree allows to lookup an object using a custom key.    * e.g. the java Map allows only to lookup by key using the Comparator    * specified in the constructor.    * In this case you can pass a specific comparator for every needs.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
interface|interface
name|AvlKeyComparator
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
block|{
name|int
name|compareKey
parameter_list|(
name|TNode
name|node
parameter_list|,
name|Object
name|key
parameter_list|)
function_decl|;
block|}
comment|/**    * Visitor that allows to traverse a set of AvlNodes.    * If you don't like the callback style of the visitor you can always use the AvlTreeIterator.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
interface|interface
name|AvlNodeVisitor
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
block|{
comment|/**      * @param node the node that we are currently visiting      * @return false to stop the iteration. true to continue.      */
name|boolean
name|visitNode
parameter_list|(
name|TNode
name|node
parameter_list|)
function_decl|;
block|}
comment|/**    * Helper class that allows to create and manipulate an AVL Tree    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
class|class
name|AvlTree
block|{
comment|/**      * @param root the current root of the tree      * @param key the key for the node we are trying to find      * @param keyComparator the comparator to use to match node and key      * @return the node that matches the specified key or null in case of node not found.      */
specifier|public
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
name|TNode
name|get
parameter_list|(
name|TNode
name|root
parameter_list|,
specifier|final
name|Object
name|key
parameter_list|,
specifier|final
name|AvlKeyComparator
argument_list|<
name|TNode
argument_list|>
name|keyComparator
parameter_list|)
block|{
while|while
condition|(
name|root
operator|!=
literal|null
condition|)
block|{
name|int
name|cmp
init|=
name|keyComparator
operator|.
name|compareKey
argument_list|(
name|root
argument_list|,
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|>
literal|0
condition|)
block|{
name|root
operator|=
operator|(
name|TNode
operator|)
name|root
operator|.
name|avlLeft
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmp
operator|<
literal|0
condition|)
block|{
name|root
operator|=
operator|(
name|TNode
operator|)
name|root
operator|.
name|avlRight
expr_stmt|;
block|}
else|else
block|{
return|return
operator|(
name|TNode
operator|)
name|root
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**      * @param root the current root of the tree      * @return the first (min) node of the tree      */
specifier|public
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
name|TNode
name|getFirst
parameter_list|(
name|TNode
name|root
parameter_list|)
block|{
if|if
condition|(
name|root
operator|!=
literal|null
condition|)
block|{
while|while
condition|(
name|root
operator|.
name|avlLeft
operator|!=
literal|null
condition|)
block|{
name|root
operator|=
operator|(
name|TNode
operator|)
name|root
operator|.
name|avlLeft
expr_stmt|;
block|}
block|}
return|return
name|root
return|;
block|}
comment|/**      * @param root the current root of the tree      * @return the last (max) node of the tree      */
specifier|public
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
name|TNode
name|getLast
parameter_list|(
name|TNode
name|root
parameter_list|)
block|{
if|if
condition|(
name|root
operator|!=
literal|null
condition|)
block|{
while|while
condition|(
name|root
operator|.
name|avlRight
operator|!=
literal|null
condition|)
block|{
name|root
operator|=
operator|(
name|TNode
operator|)
name|root
operator|.
name|avlRight
expr_stmt|;
block|}
block|}
return|return
name|root
return|;
block|}
comment|/**      * Insert a node into the tree. It uses the AvlNode.compareTo() for ordering.      * NOTE: The node must not be already in the tree.      * @param root the current root of the tree      * @param node the node to insert      * @return the new root of the tree      */
specifier|public
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
name|TNode
name|insert
parameter_list|(
name|TNode
name|root
parameter_list|,
name|TNode
name|node
parameter_list|)
block|{
if|if
condition|(
name|root
operator|==
literal|null
condition|)
return|return
name|node
return|;
name|int
name|cmp
init|=
name|node
operator|.
name|compareTo
argument_list|(
name|root
argument_list|)
decl_stmt|;
assert|assert
name|cmp
operator|!=
literal|0
operator|:
literal|"node already inserted: "
operator|+
name|root
assert|;
if|if
condition|(
name|cmp
operator|<
literal|0
condition|)
block|{
name|root
operator|.
name|avlLeft
operator|=
name|insert
argument_list|(
name|root
operator|.
name|avlLeft
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|root
operator|.
name|avlRight
operator|=
name|insert
argument_list|(
name|root
operator|.
name|avlRight
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
return|return
name|balance
argument_list|(
name|root
argument_list|)
return|;
block|}
comment|/**      * Insert a node into the tree.      * This is useful when you want to create a new node or replace the content      * depending if the node already exists or not.      * Using AvlInsertOrReplace class you can return the node to add/replace.      *      * @param root the current root of the tree      * @param key the key for the node we are trying to insert      * @param keyComparator the comparator to use to match node and key      * @param insertOrReplace the class to use to insert or replace the node      * @return the new root of the tree      */
specifier|public
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
name|TNode
name|insert
parameter_list|(
name|TNode
name|root
parameter_list|,
name|Object
name|key
parameter_list|,
specifier|final
name|AvlKeyComparator
argument_list|<
name|TNode
argument_list|>
name|keyComparator
parameter_list|,
specifier|final
name|AvlInsertOrReplace
argument_list|<
name|TNode
argument_list|>
name|insertOrReplace
parameter_list|)
block|{
if|if
condition|(
name|root
operator|==
literal|null
condition|)
block|{
return|return
name|insertOrReplace
operator|.
name|insert
argument_list|(
name|key
argument_list|)
return|;
block|}
name|int
name|cmp
init|=
name|keyComparator
operator|.
name|compareKey
argument_list|(
name|root
argument_list|,
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|<
literal|0
condition|)
block|{
name|root
operator|.
name|avlLeft
operator|=
name|insert
argument_list|(
operator|(
name|TNode
operator|)
name|root
operator|.
name|avlLeft
argument_list|,
name|key
argument_list|,
name|keyComparator
argument_list|,
name|insertOrReplace
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmp
operator|>
literal|0
condition|)
block|{
name|root
operator|.
name|avlRight
operator|=
name|insert
argument_list|(
operator|(
name|TNode
operator|)
name|root
operator|.
name|avlRight
argument_list|,
name|key
argument_list|,
name|keyComparator
argument_list|,
name|insertOrReplace
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|TNode
name|left
init|=
operator|(
name|TNode
operator|)
name|root
operator|.
name|avlLeft
decl_stmt|;
name|TNode
name|right
init|=
operator|(
name|TNode
operator|)
name|root
operator|.
name|avlRight
decl_stmt|;
name|root
operator|=
name|insertOrReplace
operator|.
name|replace
argument_list|(
name|key
argument_list|,
name|root
argument_list|)
expr_stmt|;
name|root
operator|.
name|avlLeft
operator|=
name|left
expr_stmt|;
name|root
operator|.
name|avlRight
operator|=
name|right
expr_stmt|;
return|return
name|root
return|;
block|}
return|return
name|balance
argument_list|(
name|root
argument_list|)
return|;
block|}
specifier|private
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
name|TNode
name|removeMin
parameter_list|(
name|TNode
name|p
parameter_list|)
block|{
if|if
condition|(
name|p
operator|.
name|avlLeft
operator|==
literal|null
condition|)
return|return
operator|(
name|TNode
operator|)
name|p
operator|.
name|avlRight
return|;
name|p
operator|.
name|avlLeft
operator|=
name|removeMin
argument_list|(
name|p
operator|.
name|avlLeft
argument_list|)
expr_stmt|;
return|return
name|balance
argument_list|(
name|p
argument_list|)
return|;
block|}
comment|/**      * Removes the node matching the specified key from the tree      * @param root the current root of the tree      * @param key the key for the node we are trying to find      * @param keyComparator the comparator to use to match node and key      * @return the new root of the tree      */
specifier|public
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
name|TNode
name|remove
parameter_list|(
name|TNode
name|root
parameter_list|,
name|Object
name|key
parameter_list|,
specifier|final
name|AvlKeyComparator
argument_list|<
name|TNode
argument_list|>
name|keyComparator
parameter_list|)
block|{
return|return
name|remove
argument_list|(
name|root
argument_list|,
name|key
argument_list|,
name|keyComparator
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**      * Removes the node matching the specified key from the tree      * @param root the current root of the tree      * @param key the key for the node we are trying to find      * @param keyComparator the comparator to use to match node and key      * @param removed will be set to true if the node was found and removed, otherwise false      * @return the new root of the tree      */
specifier|public
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
name|TNode
name|remove
parameter_list|(
name|TNode
name|root
parameter_list|,
name|Object
name|key
parameter_list|,
specifier|final
name|AvlKeyComparator
argument_list|<
name|TNode
argument_list|>
name|keyComparator
parameter_list|,
specifier|final
name|AtomicBoolean
name|removed
parameter_list|)
block|{
if|if
condition|(
name|root
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|int
name|cmp
init|=
name|keyComparator
operator|.
name|compareKey
argument_list|(
name|root
argument_list|,
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|removed
operator|!=
literal|null
condition|)
name|removed
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|TNode
name|q
init|=
operator|(
name|TNode
operator|)
name|root
operator|.
name|avlLeft
decl_stmt|;
name|TNode
name|r
init|=
operator|(
name|TNode
operator|)
name|root
operator|.
name|avlRight
decl_stmt|;
if|if
condition|(
name|r
operator|==
literal|null
condition|)
return|return
name|q
return|;
name|TNode
name|min
init|=
name|getFirst
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|min
operator|.
name|avlRight
operator|=
name|removeMin
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|min
operator|.
name|avlLeft
operator|=
name|q
expr_stmt|;
return|return
name|balance
argument_list|(
name|min
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|cmp
operator|>
literal|0
condition|)
block|{
name|root
operator|.
name|avlLeft
operator|=
name|remove
argument_list|(
operator|(
name|TNode
operator|)
name|root
operator|.
name|avlLeft
argument_list|,
name|key
argument_list|,
name|keyComparator
argument_list|)
expr_stmt|;
block|}
else|else
comment|/* if (cmp< 0) */
block|{
name|root
operator|.
name|avlRight
operator|=
name|remove
argument_list|(
operator|(
name|TNode
operator|)
name|root
operator|.
name|avlRight
argument_list|,
name|key
argument_list|,
name|keyComparator
argument_list|)
expr_stmt|;
block|}
return|return
name|balance
argument_list|(
name|root
argument_list|)
return|;
block|}
comment|/**      * Visit each node of the tree      * @param root the current root of the tree      * @param visitor the AvlNodeVisitor instance      */
specifier|public
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
name|void
name|visit
parameter_list|(
specifier|final
name|TNode
name|root
parameter_list|,
specifier|final
name|AvlNodeVisitor
argument_list|<
name|TNode
argument_list|>
name|visitor
parameter_list|)
block|{
if|if
condition|(
name|root
operator|==
literal|null
condition|)
return|return;
specifier|final
name|AvlTreeIterator
argument_list|<
name|TNode
argument_list|>
name|iterator
init|=
operator|new
name|AvlTreeIterator
argument_list|<>
argument_list|(
name|root
argument_list|)
decl_stmt|;
name|boolean
name|visitNext
init|=
literal|true
decl_stmt|;
while|while
condition|(
name|visitNext
operator|&&
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|visitNext
operator|=
name|visitor
operator|.
name|visitNode
argument_list|(
name|iterator
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
name|TNode
name|balance
parameter_list|(
name|TNode
name|p
parameter_list|)
block|{
name|fixHeight
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|int
name|balance
init|=
name|balanceFactor
argument_list|(
name|p
argument_list|)
decl_stmt|;
if|if
condition|(
name|balance
operator|==
literal|2
condition|)
block|{
if|if
condition|(
name|balanceFactor
argument_list|(
name|p
operator|.
name|avlRight
argument_list|)
operator|<
literal|0
condition|)
block|{
name|p
operator|.
name|avlRight
operator|=
name|rotateRight
argument_list|(
name|p
operator|.
name|avlRight
argument_list|)
expr_stmt|;
block|}
return|return
name|rotateLeft
argument_list|(
name|p
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|balance
operator|==
operator|-
literal|2
condition|)
block|{
if|if
condition|(
name|balanceFactor
argument_list|(
name|p
operator|.
name|avlLeft
argument_list|)
operator|>
literal|0
condition|)
block|{
name|p
operator|.
name|avlLeft
operator|=
name|rotateLeft
argument_list|(
name|p
operator|.
name|avlLeft
argument_list|)
expr_stmt|;
block|}
return|return
name|rotateRight
argument_list|(
name|p
argument_list|)
return|;
block|}
return|return
name|p
return|;
block|}
specifier|private
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
name|TNode
name|rotateRight
parameter_list|(
name|TNode
name|p
parameter_list|)
block|{
name|TNode
name|q
init|=
operator|(
name|TNode
operator|)
name|p
operator|.
name|avlLeft
decl_stmt|;
name|p
operator|.
name|avlLeft
operator|=
name|q
operator|.
name|avlRight
expr_stmt|;
name|q
operator|.
name|avlRight
operator|=
name|p
expr_stmt|;
name|fixHeight
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|fixHeight
argument_list|(
name|q
argument_list|)
expr_stmt|;
return|return
name|q
return|;
block|}
specifier|private
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
name|TNode
name|rotateLeft
parameter_list|(
name|TNode
name|q
parameter_list|)
block|{
name|TNode
name|p
init|=
operator|(
name|TNode
operator|)
name|q
operator|.
name|avlRight
decl_stmt|;
name|q
operator|.
name|avlRight
operator|=
name|p
operator|.
name|avlLeft
expr_stmt|;
name|p
operator|.
name|avlLeft
operator|=
name|q
expr_stmt|;
name|fixHeight
argument_list|(
name|q
argument_list|)
expr_stmt|;
name|fixHeight
argument_list|(
name|p
argument_list|)
expr_stmt|;
return|return
name|p
return|;
block|}
specifier|private
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
name|void
name|fixHeight
parameter_list|(
name|TNode
name|node
parameter_list|)
block|{
specifier|final
name|int
name|heightLeft
init|=
name|height
argument_list|(
name|node
operator|.
name|avlLeft
argument_list|)
decl_stmt|;
specifier|final
name|int
name|heightRight
init|=
name|height
argument_list|(
name|node
operator|.
name|avlRight
argument_list|)
decl_stmt|;
name|node
operator|.
name|avlHeight
operator|=
literal|1
operator|+
name|Math
operator|.
name|max
argument_list|(
name|heightLeft
argument_list|,
name|heightRight
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
name|int
name|height
parameter_list|(
name|TNode
name|node
parameter_list|)
block|{
return|return
name|node
operator|!=
literal|null
condition|?
name|node
operator|.
name|avlHeight
else|:
literal|0
return|;
block|}
specifier|private
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
name|int
name|balanceFactor
parameter_list|(
name|TNode
name|node
parameter_list|)
block|{
return|return
name|height
argument_list|(
name|node
operator|.
name|avlRight
argument_list|)
operator|-
name|height
argument_list|(
name|node
operator|.
name|avlLeft
argument_list|)
return|;
block|}
block|}
comment|/**    * Iterator for the AvlTree    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
class|class
name|AvlTreeIterator
parameter_list|<
name|TNode
extends|extends
name|AvlNode
parameter_list|>
implements|implements
name|Iterator
argument_list|<
name|TNode
argument_list|>
block|{
specifier|private
specifier|final
name|Object
index|[]
name|stack
init|=
operator|new
name|Object
index|[
literal|64
index|]
decl_stmt|;
specifier|private
name|TNode
name|current
init|=
literal|null
decl_stmt|;
specifier|private
name|int
name|height
init|=
literal|0
decl_stmt|;
specifier|public
name|AvlTreeIterator
parameter_list|()
block|{     }
comment|/**      * Create the iterator starting from the first (min) node of the tree      */
specifier|public
name|AvlTreeIterator
parameter_list|(
specifier|final
name|TNode
name|root
parameter_list|)
block|{
name|seekFirst
argument_list|(
name|root
argument_list|)
expr_stmt|;
block|}
comment|/**      * Create the iterator starting from the specified key      * @param root the current root of the tree      * @param key the key for the node we are trying to find      * @param keyComparator the comparator to use to match node and key      */
specifier|public
name|AvlTreeIterator
parameter_list|(
specifier|final
name|TNode
name|root
parameter_list|,
specifier|final
name|Object
name|key
parameter_list|,
specifier|final
name|AvlKeyComparator
argument_list|<
name|TNode
argument_list|>
name|keyComparator
parameter_list|)
block|{
name|seekTo
argument_list|(
name|root
argument_list|,
name|key
argument_list|,
name|keyComparator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|current
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|TNode
name|next
parameter_list|()
block|{
specifier|final
name|TNode
name|node
init|=
name|this
operator|.
name|current
decl_stmt|;
name|seekNext
argument_list|()
expr_stmt|;
return|return
name|node
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|/**      * Reset the iterator, and seeks to the first (min) node of the tree      * @param root the current root of the tree      */
specifier|public
name|void
name|seekFirst
parameter_list|(
specifier|final
name|TNode
name|root
parameter_list|)
block|{
name|current
operator|=
name|root
expr_stmt|;
name|height
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|root
operator|!=
literal|null
condition|)
block|{
while|while
condition|(
name|current
operator|.
name|avlLeft
operator|!=
literal|null
condition|)
block|{
name|stack
index|[
name|height
operator|++
index|]
operator|=
name|current
expr_stmt|;
name|current
operator|=
operator|(
name|TNode
operator|)
name|current
operator|.
name|avlLeft
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Reset the iterator, and seeks to the specified key      * @param root the current root of the tree      * @param key the key for the node we are trying to find      * @param keyComparator the comparator to use to match node and key      */
specifier|public
name|void
name|seekTo
parameter_list|(
specifier|final
name|TNode
name|root
parameter_list|,
specifier|final
name|Object
name|key
parameter_list|,
specifier|final
name|AvlKeyComparator
argument_list|<
name|TNode
argument_list|>
name|keyComparator
parameter_list|)
block|{
name|current
operator|=
literal|null
expr_stmt|;
name|height
operator|=
literal|0
expr_stmt|;
name|TNode
name|node
init|=
name|root
decl_stmt|;
while|while
condition|(
name|node
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|keyComparator
operator|.
name|compareKey
argument_list|(
name|node
argument_list|,
name|key
argument_list|)
operator|>=
literal|0
condition|)
block|{
if|if
condition|(
name|node
operator|.
name|avlLeft
operator|!=
literal|null
condition|)
block|{
name|stack
index|[
name|height
operator|++
index|]
operator|=
name|node
expr_stmt|;
name|node
operator|=
operator|(
name|TNode
operator|)
name|node
operator|.
name|avlLeft
expr_stmt|;
block|}
else|else
block|{
name|current
operator|=
name|node
expr_stmt|;
return|return;
block|}
block|}
else|else
block|{
if|if
condition|(
name|node
operator|.
name|avlRight
operator|!=
literal|null
condition|)
block|{
name|stack
index|[
name|height
operator|++
index|]
operator|=
name|node
expr_stmt|;
name|node
operator|=
operator|(
name|TNode
operator|)
name|node
operator|.
name|avlRight
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|height
operator|>
literal|0
condition|)
block|{
name|TNode
name|parent
init|=
operator|(
name|TNode
operator|)
name|stack
index|[
operator|--
name|height
index|]
decl_stmt|;
while|while
condition|(
name|node
operator|==
name|parent
operator|.
name|avlRight
condition|)
block|{
if|if
condition|(
name|height
operator|==
literal|0
condition|)
block|{
name|current
operator|=
literal|null
expr_stmt|;
return|return;
block|}
name|node
operator|=
name|parent
expr_stmt|;
name|parent
operator|=
operator|(
name|TNode
operator|)
name|stack
index|[
operator|--
name|height
index|]
expr_stmt|;
block|}
name|current
operator|=
name|parent
expr_stmt|;
return|return;
block|}
name|current
operator|=
literal|null
expr_stmt|;
return|return;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|seekNext
parameter_list|()
block|{
if|if
condition|(
name|current
operator|==
literal|null
condition|)
return|return;
if|if
condition|(
name|current
operator|.
name|avlRight
operator|!=
literal|null
condition|)
block|{
name|stack
index|[
name|height
operator|++
index|]
operator|=
name|current
expr_stmt|;
name|current
operator|=
operator|(
name|TNode
operator|)
name|current
operator|.
name|avlRight
expr_stmt|;
while|while
condition|(
name|current
operator|.
name|avlLeft
operator|!=
literal|null
condition|)
block|{
name|stack
index|[
name|height
operator|++
index|]
operator|=
name|current
expr_stmt|;
name|current
operator|=
operator|(
name|TNode
operator|)
name|current
operator|.
name|avlLeft
expr_stmt|;
block|}
block|}
else|else
block|{
name|TNode
name|node
decl_stmt|;
do|do
block|{
if|if
condition|(
name|height
operator|==
literal|0
condition|)
block|{
name|current
operator|=
literal|null
expr_stmt|;
return|return;
block|}
name|node
operator|=
name|current
expr_stmt|;
name|current
operator|=
operator|(
name|TNode
operator|)
name|stack
index|[
operator|--
name|height
index|]
expr_stmt|;
block|}
do|while
condition|(
name|current
operator|.
name|avlRight
operator|==
name|node
condition|)
do|;
block|}
block|}
block|}
comment|/**    * Helper class that allows to create and manipulate a linked list of AvlLinkedNodes    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
class|class
name|AvlIterableList
block|{
comment|/**      * @param node the current node      * @return the successor of the current node      */
specifier|public
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlLinkedNode
parameter_list|>
name|TNode
name|readNext
parameter_list|(
name|TNode
name|node
parameter_list|)
block|{
return|return
operator|(
name|TNode
operator|)
name|node
operator|.
name|iterNext
return|;
block|}
comment|/**      * @param node the current node      * @return the predecessor of the current node      */
specifier|public
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlLinkedNode
parameter_list|>
name|TNode
name|readPrev
parameter_list|(
name|TNode
name|node
parameter_list|)
block|{
return|return
operator|(
name|TNode
operator|)
name|node
operator|.
name|iterPrev
return|;
block|}
comment|/**      * @param head the head of the linked list      * @param node the node to add to the front of the list      * @return the new head of the list      */
specifier|public
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlLinkedNode
parameter_list|>
name|TNode
name|prepend
parameter_list|(
name|TNode
name|head
parameter_list|,
name|TNode
name|node
parameter_list|)
block|{
assert|assert
operator|!
name|isLinked
argument_list|(
name|node
argument_list|)
operator|:
name|node
operator|+
literal|" is already linked"
assert|;
if|if
condition|(
name|head
operator|!=
literal|null
condition|)
block|{
name|TNode
name|tail
init|=
operator|(
name|TNode
operator|)
name|head
operator|.
name|iterPrev
decl_stmt|;
name|tail
operator|.
name|iterNext
operator|=
name|node
expr_stmt|;
name|head
operator|.
name|iterPrev
operator|=
name|node
expr_stmt|;
name|node
operator|.
name|iterNext
operator|=
name|head
expr_stmt|;
name|node
operator|.
name|iterPrev
operator|=
name|tail
expr_stmt|;
block|}
else|else
block|{
name|node
operator|.
name|iterNext
operator|=
name|node
expr_stmt|;
name|node
operator|.
name|iterPrev
operator|=
name|node
expr_stmt|;
block|}
return|return
name|node
return|;
block|}
comment|/**      * @param head the head of the linked list      * @param node the node to add to the tail of the list      * @return the new head of the list      */
specifier|public
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlLinkedNode
parameter_list|>
name|TNode
name|append
parameter_list|(
name|TNode
name|head
parameter_list|,
name|TNode
name|node
parameter_list|)
block|{
assert|assert
operator|!
name|isLinked
argument_list|(
name|node
argument_list|)
operator|:
name|node
operator|+
literal|" is already linked"
assert|;
if|if
condition|(
name|head
operator|!=
literal|null
condition|)
block|{
name|TNode
name|tail
init|=
operator|(
name|TNode
operator|)
name|head
operator|.
name|iterPrev
decl_stmt|;
name|tail
operator|.
name|iterNext
operator|=
name|node
expr_stmt|;
name|node
operator|.
name|iterNext
operator|=
name|head
expr_stmt|;
name|node
operator|.
name|iterPrev
operator|=
name|tail
expr_stmt|;
name|head
operator|.
name|iterPrev
operator|=
name|node
expr_stmt|;
return|return
name|head
return|;
block|}
name|node
operator|.
name|iterNext
operator|=
name|node
expr_stmt|;
name|node
operator|.
name|iterPrev
operator|=
name|node
expr_stmt|;
return|return
name|node
return|;
block|}
comment|/**      * @param head the head of the current linked list      * @param otherHead the head of the list to append to the current list      * @return the new head of the current list      */
specifier|public
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlLinkedNode
parameter_list|>
name|TNode
name|appendList
parameter_list|(
name|TNode
name|head
parameter_list|,
name|TNode
name|otherHead
parameter_list|)
block|{
if|if
condition|(
name|head
operator|==
literal|null
condition|)
return|return
name|otherHead
return|;
if|if
condition|(
name|otherHead
operator|==
literal|null
condition|)
return|return
name|head
return|;
name|TNode
name|tail
init|=
operator|(
name|TNode
operator|)
name|head
operator|.
name|iterPrev
decl_stmt|;
name|TNode
name|otherTail
init|=
operator|(
name|TNode
operator|)
name|otherHead
operator|.
name|iterPrev
decl_stmt|;
name|tail
operator|.
name|iterNext
operator|=
name|otherHead
expr_stmt|;
name|otherHead
operator|.
name|iterPrev
operator|=
name|tail
expr_stmt|;
name|otherTail
operator|.
name|iterNext
operator|=
name|head
expr_stmt|;
name|head
operator|.
name|iterPrev
operator|=
name|otherTail
expr_stmt|;
return|return
name|head
return|;
block|}
comment|/**      * @param head the head of the linked list      * @param node the node to remove from the list      * @return the new head of the list      */
specifier|public
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlLinkedNode
parameter_list|>
name|TNode
name|remove
parameter_list|(
name|TNode
name|head
parameter_list|,
name|TNode
name|node
parameter_list|)
block|{
assert|assert
name|isLinked
argument_list|(
name|node
argument_list|)
operator|:
name|node
operator|+
literal|" is not linked"
assert|;
if|if
condition|(
name|node
operator|!=
name|node
operator|.
name|iterNext
condition|)
block|{
name|node
operator|.
name|iterPrev
operator|.
name|iterNext
operator|=
name|node
operator|.
name|iterNext
expr_stmt|;
name|node
operator|.
name|iterNext
operator|.
name|iterPrev
operator|=
name|node
operator|.
name|iterPrev
expr_stmt|;
name|head
operator|=
operator|(
name|head
operator|==
name|node
operator|)
condition|?
operator|(
name|TNode
operator|)
name|node
operator|.
name|iterNext
else|:
name|head
expr_stmt|;
block|}
else|else
block|{
name|head
operator|=
literal|null
expr_stmt|;
block|}
name|node
operator|.
name|iterNext
operator|=
literal|null
expr_stmt|;
name|node
operator|.
name|iterPrev
operator|=
literal|null
expr_stmt|;
return|return
name|head
return|;
block|}
comment|/**      * @param node the node to check      * @return true if the node is linked to a list, false otherwise      */
specifier|public
specifier|static
parameter_list|<
name|TNode
extends|extends
name|AvlLinkedNode
parameter_list|>
name|boolean
name|isLinked
parameter_list|(
name|TNode
name|node
parameter_list|)
block|{
return|return
name|node
operator|.
name|iterPrev
operator|!=
literal|null
operator|&&
name|node
operator|.
name|iterNext
operator|!=
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

